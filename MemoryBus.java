public class MemoryBus {
    private int[] memory;

    public MemoryBus(int size) {
        memory = new int[size / 4];
    }

    public int read(int address) {
        return memory[address / 4];
    }

    public void write(int address, int data) {
        memory[address / 4] = data;
    }
}