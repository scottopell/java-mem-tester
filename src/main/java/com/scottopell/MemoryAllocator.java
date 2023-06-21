package com.scottopell;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.Queue;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class MemoryAllocator {
    private static final int MEMORY_BLOCK_SIZE = 10 * 1024 * 1024; // 10 MB

    private static Queue<byte[]> memory = new LinkedList<byte[]>();
    private static long allocatedMemory = 0;

    public static void main(String[] args) throws IOException {
        System.out.println("Press 'a' to allocate memory, 'A' to allocate 10x memory, 'f' to free memory, or 'q' to quit.");

        Terminal terminal = TerminalBuilder.terminal();
        terminal.enterRawMode();
        try {
            while (true) {
                char ch = (char) terminal.reader().read();
                switch (ch) {
                    case 'a':
                        allocateMemory(1);
                        break;
                    case 'A':
                        allocateMemory(10);
                        break;
                    case 'f':
                        freeMemory();
                        break;
                    case 'q':
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid input. Press 'a' to allocate memory, 'f' to free memory, or 'q' to quit.");
            }

            printMemoryInfoBean();
            }
        } finally {
            terminal.close();
        }
    }

    private static void allocateMemory(int multiplier) {
        byte[] memoryBlock = new byte[MEMORY_BLOCK_SIZE * multiplier];
        memory.add(memoryBlock);
        allocatedMemory += MEMORY_BLOCK_SIZE * multiplier;
    }

    private static void freeMemory() {
        allocatedMemory -= MEMORY_BLOCK_SIZE;
        memory.remove();
        System.gc(); // Request garbage collection to reclaim freed memory
    }

    private static void printMemoryInfoRuntime() {
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Allocated Memory: " + allocatedMemory);
        System.out.println("Used Memory: " + usedMemory);
        System.out.println("Total Memory: " + totalMemory);
        System.out.println("Free Memory: " + freeMemory);
        System.out.println();
    }
    public static void printMemoryInfoBean() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();


        System.out.println("Heap Memory Usage:");
        System.out.println("    Initial: " + formatBytes(heapMemoryUsage.getInit()));
        System.out.println("    Used: " + formatBytes(heapMemoryUsage.getUsed()));
        System.out.println("    Committed: " + formatBytes(heapMemoryUsage.getCommitted()));
        System.out.println("    Max: " + formatBytes(heapMemoryUsage.getMax()));

        System.out.println("Non-Heap Memory Usage:");
        System.out.println("    Initial: " + formatBytes(nonHeapMemoryUsage.getInit()));
        System.out.println("    Used: " + formatBytes(nonHeapMemoryUsage.getUsed()));
        System.out.println("    Committed: " + formatBytes(nonHeapMemoryUsage.getCommitted()));
        System.out.println("    Max: " + formatBytes(nonHeapMemoryUsage.getMax()));
    }

    private static String formatBytes(long bytes) {
        DecimalFormat df = new DecimalFormat("#.##");

        double kilobytes = bytes / 1024.0;
        double megabytes = kilobytes / 1024.0;
        double gigabytes = megabytes / 1024.0;

        if (gigabytes >= 1) {
            return df.format(gigabytes) + " GB";
        } else if (megabytes >= 1) {
            return df.format(megabytes) + " MB";
        } else if (kilobytes >= 1) {
            return df.format(kilobytes) + " KB";
        } else {
            return bytes + " bytes";
        }
    }

}

