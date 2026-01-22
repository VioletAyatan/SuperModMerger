package ankol.mod.merger.gui.component;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 自动更新的时间标签组件
 * 类似Vue的响应式组件，自动刷新显示当前时间
 * <p>
 * 使用示例：
 * <pre>
 * // 显示时分秒
 * TimeLabel timeLabel = new TimeLabel("HH:mm:ss");
 *
 * // 显示完整日期时间
 * TimeLabel dateTimeLabel = new TimeLabel("yyyy-MM-dd HH:mm:ss");
 *
 * // 使用后记得停止
 * timeLabel.stop();
 * </pre>
 */
public class TimeLabel extends Label {
    private final String pattern;
    private final StringProperty timeProperty;
    private final Timeline timeline;
    private final DateTimeFormatter formatter;

    /**
     * 创建时间标签
     *
     * @param pattern 时间格式，如 "HH:mm:ss"、"yyyy-MM-dd HH:mm:ss" 等
     */
    public TimeLabel(String pattern) {
        this.pattern = pattern;
        this.formatter = DateTimeFormatter.ofPattern(pattern);

        // 初始化响应式时间属性
        this.timeProperty = new SimpleStringProperty(getCurrentTimeString());

        // 绑定属性到Label的文本
        textProperty().bind(timeProperty);

        // 创建定时器，每秒更新一次
        this.timeline = new Timeline(
                new KeyFrame(Duration.seconds(1.0), event -> updateTime())
        );
        timeline.setCycleCount(Animation.INDEFINITE);

        // 自动启动
        start();
    }

    /**
     * 使用默认格式 "HH:mm:ss" 创建时间标签
     */
    public TimeLabel() {
        this("HH:mm:ss");
    }

    /**
     * 获取当前时间字符串
     */
    private String getCurrentTimeString() {
        return LocalDateTime.now().format(formatter);
    }

    /**
     * 更新时间
     */
    private void updateTime() {
        timeProperty.set(getCurrentTimeString());
    }

    /**
     * 启动自动更新
     */
    public void start() {
        if (timeline.getStatus() != Animation.Status.RUNNING) {
            timeline.play();
        }
    }

    /**
     * 停止自动更新
     */
    public void stop() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.stop();
        }
    }

    /**
     * 暂停自动更新
     */
    public void pause() {
        if (timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.pause();
        }
    }

    /**
     * 恢复自动更新
     */
    public void resume() {
        if (timeline.getStatus() == Animation.Status.PAUSED) {
            timeline.play();
        }
    }

    /**
     * 获取时间格式
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * 获取时间属性（用于自定义绑定）
     */
    public StringProperty timeProperty() {
        return timeProperty;
    }

    /**
     * 设置更新频率
     *
     * @param seconds 更新间隔（秒）
     */
    public void setUpdateInterval(double seconds) {
        boolean wasRunning = timeline.getStatus() == Animation.Status.RUNNING;
        stop();

        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(seconds), event -> updateTime())
        );

        if (wasRunning) {
            start();
        }
    }
}
