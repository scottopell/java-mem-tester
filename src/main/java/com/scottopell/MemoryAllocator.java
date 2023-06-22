package com.scottopell;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class MemoryAllocator {
    private static final int MEMORY_BLOCK_SIZE = 10 * 1024 * 1024; // 10 MB

    private static HashMap<Integer, Queue<byte[]>> memory = new HashMap<>();
    private static long allocatedMemory = 0;

    public static void main(String[] args) throws IOException {
        String helpMessage = "Options:\n" +
            "'a' - allocate 10MB chunk of memory\n" +
            "'A' - allocate 100MB chunk of memory\n" +
            "'f' - free 10MB chunk of memory\n" +
            "'F' - free 100MB chunk of memory\n" +
            "'?' - print this help message\n" +
            "'i' - print detailed memory statistics\n" +
            "'q' - quit program";
        System.out.println("Welcome to the java memory tester. " + helpMessage);

        Terminal terminal = TerminalBuilder.terminal();
        terminal.enterRawMode();
        try {
            while (true) {
                char ch = (char) terminal.reader().read();
                boolean memChanged = false;
                switch (ch) {
                    case 'a':
                        allocateMemory(1);
                        memChanged = true;
                        break;
                    case 'A':
                        allocateMemory(10);
                        memChanged = true;
                        break;
                    case 'f':
                        freeMemory(1);
                        memChanged = true;
                        break;
                    case 'F':
                        freeMemory(10);
                        memChanged = true;
                        break;
                    case '?':
                        System.out.println(helpMessage);
                        break;
                    case 'i':
                        printMemoryInfoBean(true);
                        break;
                    case 'q':
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid input. " + helpMessage);
                }

                if (memChanged) {
                    printMemoryInfoBean(false);
                }
                System.out.println("=".repeat(terminal.getWidth()));
            }
        } finally {
            terminal.close();
        }
    }

    private static void allocateMemory(int multiplier) {
        int allocSize = MEMORY_BLOCK_SIZE * multiplier;
        try {
            byte[] memoryBlock = new byte[allocSize];
            memory.computeIfAbsent(allocSize, k -> new LinkedList<>()).add(memoryBlock);
            allocatedMemory += allocSize;
        } catch (java.lang.OutOfMemoryError e) {
            System.err.println("Out-of-memory, allocation of " + formatBytes(MEMORY_BLOCK_SIZE * multiplier) + " failed.");
        }
    }

    private static void freeMemory(int multiplier) {
        int allocSize = MEMORY_BLOCK_SIZE * multiplier;
        if (memory.containsKey(allocSize)) {
            if (memory.get(allocSize).poll() != null) {
                allocatedMemory -= MEMORY_BLOCK_SIZE;
            }
        }
        System.gc(); // Request garbage collection to reclaim freed memory
    }

    public static void printMemoryInfoBean(boolean verbose) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();


        System.out.println("Heap Memory Usage:");
        System.out.println("    Used: " + formatBytes(heapMemoryUsage.getUsed()));

        if (verbose) {
            System.out.println("    Initial: " + formatBytes(heapMemoryUsage.getInit()));
            System.out.println("    Max: " + formatBytes(heapMemoryUsage.getMax()));
            System.out.println("    Committed: " + formatBytes(heapMemoryUsage.getCommitted()));

            System.out.println("Non-Heap Memory Usage:");
            System.out.println("    Initial: " + formatBytes(nonHeapMemoryUsage.getInit()));
            System.out.println("    Used: " + formatBytes(nonHeapMemoryUsage.getUsed()));
            System.out.println("    Committed: " + formatBytes(nonHeapMemoryUsage.getCommitted()));
            System.out.println("    Max: " + formatBytes(nonHeapMemoryUsage.getMax()));
        }
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

