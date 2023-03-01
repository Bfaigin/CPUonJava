public class RegisterFile {
    private int[] registers;

    public RegisterFile(int numRegisters) {
        registers = new int[numRegisters];
    }

    public int read(int index) {
        return registers[index];
    }

    public void write(int index, int value) {
        registers[index] = value;
    }
}