public class Cache {
    private int size;
    private int blockSize;
    private int numBlocks;
    private int[][] cache;
    private boolean enabled;
    private MemoryBus memoryBus;

    public Cache(int size, int blockSize, MemoryBus memoryBus) {
        this.size = size;
        this.blockSize = blockSize;
        this.numBlocks = size / blockSize;
        this.cache = new int[numBlocks][blockSize / 4];
        this.enabled = true;
        this.memoryBus = memoryBus;
    }

    public int read(int address) {
        if (!enabled) {
            return memoryBus.read(address);
        }
        int tag = address / blockSize;
        int index = tag % numBlocks;
        int offset = address % blockSize;
        if (offset + 4 > blockSize) {
            int data1 = read(address & ~(blockSize - 1));
            int data2 = read(address & ~(blockSize - 1) + blockSize);
            int data = (data1 << (blockSize - offset)) | (data2 >>> offset);
            return data;
        } else {
            return cache[index][offset / 4];
        }
    }

    public void write(int address, int data) {
        if (!enabled) {
            memoryBus.write(address, data);
            return;
        }
        int tag = address / blockSize;
        int index = tag % numBlocks;
        int offset = address % blockSize;
        if (offset + 4 > blockSize) {
            int data1 = data >>> (blockSize - offset);
            int data2 = data << offset;
            write(address & ~(blockSize - 1), data1);
            write(address & ~(blockSize - 1) + blockSize, data2);
        } else {
            cache[index][offset / 4] = data;
        }
    }

    public void enable() {
        enabled = true;
    }

    public void disable() {
        enabled = false;
    }

    public void flush() {
        cache = new int[numBlocks][blockSize / 4];
    }
}