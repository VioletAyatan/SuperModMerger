package ankol.mod.merger.tools;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.*;

/**
 * 对当前系统的线程池操作进行简单封装的工具类
 * <p>
 * 基于单例模式实现
 *
 * @author lichengkun
 */
@Slf4j
public abstract class AsyncUtil {
    private static final ScheduledExecutorService executorService;

    static {
        //初始化调度线程池
        executorService = new ScheduledThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 2,
                BasicThreadFactory.builder().namingPattern("schedule-pool-%d").uncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        log.error("线程池执行任务时出现异常，线程名称：" + t.getName() + " 异常原因：" + e.getMessage(), e);
                    }
                }).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 执行给定的任务
     *
     * @param runnable 任务
     */
    public static void execute(Runnable runnable) {
        executorService.execute(runnable);
    }

    /**
     * 提交一次性任务，该任务将在给定延迟后执行
     *
     * @param task     任务
     * @param delay    延迟时间
     * @param timeUnit 时间单位
     * @return {@link ScheduledFuture}
     */
    public static ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit timeUnit) {
        return executorService.schedule(task, delay, timeUnit);
    }

    /**
     * @param task         任务
     * @param initialDelay 初始延迟
     * @param period       时间周期
     * @param timeUnit     时间周期
     * @return {@link ScheduledFuture}
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit timeUnit) {
        return executorService.scheduleAtFixedRate(task, initialDelay, period, timeUnit);
    }
}
