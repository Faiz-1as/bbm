package com.bomberman.util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 音频管理器：单例。基于 javax.sound.sampled（Swing 技术栈，取代 JavaFX MediaPlayer）。
 *
 * 通过依赖 mp3spi（见 pom.xml）为 javax.sound 注册 MP3 SPI，可直接播放 .mp3。
 * SFX 用 Clip 预加载；BGM 用循环 Clip。加载失败则优雅跳过（游戏照常运行）。
 */
public class AudioManager {
    private static AudioManager instance;

    private final Map<String, Clip> sfxClips = new HashMap<>();
    private Clip bgmClip;
    private boolean soundEnabled = true;

    private AudioManager() {
        loadSfx("bomb", "sounds/explode.mp3");
        loadSfx("explode", "sounds/explode.mp3");
        loadSfx("level_complete", "sounds/level_complete.mp3");
        loadSfx("mission_failed", "sounds/mission_failed.mp3");
        loadSfx("prop", "sounds/prop.mp3");
        loadBgm("sounds/music.mp3");
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadSfx(String name, String path) {
        Clip clip = loadClip(path);
        if (clip != null) sfxClips.put(name, clip);
    }

    private void loadBgm(String path) {
        bgmClip = loadClip(path);
        // 先备好，等 playBGM 触发循环
    }

    /**
     * 加载音频为 Clip。mp3 需先转 PCM_SIGNED（mp3spi 的原始流 frame size 为 NOT_SPECIFIED，
     * Clip.open 会报错），转换后即可正常播放。
     */
    private Clip loadClip(String path) {
        try {
            URL url = getClass().getClassLoader().getResource(path);
            if (url == null) {
                System.err.println("[AudioManager] 缺少音频: " + path);
                return null;
            }
            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            AudioFormat base = in.getFormat();
            AudioFormat decoded = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    base.getSampleRate(),
                    16,
                    base.getChannels(),
                    base.getChannels() * 2,
                    base.getSampleRate(),
                    false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decoded, in);
            Clip clip = AudioSystem.getClip();
            clip.open(din);
            din.close();
            in.close();
            return clip;
        } catch (Exception e) {
            System.err.println("[AudioManager] 加载音频失败 " + path + ": " + e.getMessage());
            return null;
        }
    }

    public void playBGM() {
        if (bgmClip != null && soundEnabled) {
            bgmClip.setFramePosition(0);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
        }
    }

    public void playSound(String name) {
        if (!soundEnabled) return;
        Clip clip = sfxClips.get(name);
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        // 通过监听在结束时停止，避免多次叠加耗尽线路
        clip.start();
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
        if (!enabled) stopBGM();
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    /** 释放资源（程序退出时调用） */
    public void close() {
        for (Clip c : sfxClips.values()) {
            if (c != null) c.close();
        }
        if (bgmClip != null) bgmClip.close();
    }
}
