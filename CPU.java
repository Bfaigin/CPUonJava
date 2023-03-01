public class CPU {
    private RegisterFile registerFile;
    private MemoryBus memoryBus;
    private Cache cache;
    private int pc;

    public CPU(int numRegisters, int cacheSize, int cacheBlockSize, int memorySize) {
        registerFile = new RegisterFile(numRegisters);
        memoryBus = new MemoryBus(memorySize);
        cache = new Cache(cacheSize, cacheBlockSize, memoryBus);
        pc = 0;
    }

    public void run() {
        while (true) {
            int instruction = cache.read(pc);
            int opcode = (instruction >> 26) & 0x3F;
            int rs = (instruction >> 21) & 0x1F;
            int rt = (instruction >> 16) & 0x1F;
            int rd = (instruction >> 11) & 0x1F;
            int immd = instruction & 0xFFFF;
            int offset = instruction & 0xFFFF;
            if ((offset & 0x8000) != 0) {
                offset |= 0xFFFF0000;
            }
            int target = instruction & 0x3FFFFFF;
            if ((target & 0x2000000) != 0) {
                target |= 0xFC000000;
            }
            switch (opcode) {
                case 0x00: // R-type instruction
                    int funct = instruction & 0x3F;
                    switch (funct) {
                        case 0x20, 0x21: // ADD
                            registerFile.write(rd, registerFile.read(rs) + registerFile.read(rt));
                            pc += 4;
                            break;
                        case 0x22: // SUB
                            registerFile.write(rd, registerFile.read(rs) - registerFile.read(rt));
                            pc += 4;
                            break;
                        case 0x2A: // SLT
                            if (registerFile.read(rs) < registerFile.read(rt)) {
                                registerFile.write(rd, 1);
                            } else {
                                registerFile.write(rd, 0);
                            }
                            pc += 4;
                            break;
                        default:
                            System.out.println("Unsupported instruction: " + Integer.toHexString(instruction));
                            return;
                    }
                    break;
                case 0x08: // ADDI
                    registerFile.write(rt, registerFile.read(rs) + immd);
                    pc += 4;
                    break;
                case 0x23: // LW
                    int address = registerFile.read(rs) + offset;
                    int data = cache.read(address);
                    registerFile.write(rt, data);
                    pc += 4;
                    break;
                case 0x2B: // SW
                    address = registerFile.read(rs) + offset;
                    data = registerFile.read(rt);
                    cache.write(address, data);
                    pc += 4;
                    break;
                case 0x04: // BNE
                    if (registerFile.read(rs) != registerFile.read(rt)) {
                        pc = pc + 4 + offset * 4;
                    } else {
                        pc += 4;
                    }
                    break;
                case 0x02: // J
                    pc = target * 4;
                    break;
                case 0x03: // JAL
                    registerFile.write(7, pc + 4);
                    pc = target * 4;
                    break;
                case 0x0F: // CACHE
                    switch (rt) {
                        case 0:
                            cache.disable();
                            break;
                        case 1:
                            cache.enable();
                            break;
                        case 2:
                            cache.flush();
                            break;
                        default:
                            System.out.println("Unsupported instruction: " + Integer.toHexString(instruction));
                            return;
                    }
                    pc += 4;
                    break;
                case 0x3F: // HALT
                    return;
                default:
                    System.out.println("Unsupported instruction: " + Integer.toHexString(instruction));
                    return;
            }
        }
    }
}