package me.cocolennon.rtpmenu.util;

import me.cocolennon.rtpmenu.Main;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;

import java.util.concurrent.atomic.AtomicInteger;

public class MetricsUtil {
    public final static AtomicInteger rtpCounter = new AtomicInteger(0);

    public static void register(Main instance) {
        Metrics metrics = new Metrics(instance, 31293);
        metrics.addCustomChart(new SingleLineChart("rtps", () -> rtpCounter.getAndSet(0)));
    }
}
