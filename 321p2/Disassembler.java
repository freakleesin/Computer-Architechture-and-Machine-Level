import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

//------------------------How to Execute------------------------
//sh build.sh
//sh run.sh <legv8 assembly file>


//-------------------------Disassembler-------------------------

class Instruction {

    int opcode;

}

class RInstruction extends Instruction {

    int Rm;
    int shamt;
    int Rn;
    int Rd;


    public RInstruction(int inst) {

        Rd = inst & 0b11111;

        Rn = (inst >> 5) & 0b11111;

        shamt = (inst >> 10) & 0b111111;

        Rm = (inst >> 16) & 0b11111;

        opcode = (inst >> 21) & 0b11111111111;
    }
}


class IInstruction extends Instruction {

    int imm;
    int Rn;
    int Rd;

    public IInstruction(int inst) {

        Rd = inst & 0b11111;

        Rn = (inst >> 5) & 0b11111;

        imm = (inst >> 10) & 0b111111111111;

        if (imm >= 0b100000000000) {

            imm -= (1 << 12);
        }

        opcode = (inst >> 22) & 0b1111111111;


    }
}

class DInstruction extends Instruction {

    int dt;

    int op;

    int Rn;

    int Rt;

    public DInstruction(int inst) {

        Rt = inst & 0b11111;

        Rn = (inst >> 5) & 0b11111;

        op = (inst >> 10) & 0b11;

        dt = (inst >> 12) & 0b111111111;

        if (dt >= 0b100000000) {

            dt -= 1 << 9;
        }

        opcode = (inst >> 21) & 0b11111111111;
    }


}

class CBInstruction extends Instruction {

    int cond_br;

    int Rt;

    public CBInstruction(int inst) {

        Rt = inst & 0b11111;

        cond_br = (inst >> 5) & 0b1111111111111111111;

        if (cond_br >= 0b1000000000000000000) {

            cond_br -= 1 << 19;
        }

        opcode = (inst >> 24) & 0b11111111;

    }

}

class BInstruction extends Instruction {

    int br;

    public BInstruction(int inst) {

        br = inst & 0b11111111111111111111111111;

        if (br >= 0b10000000000000000000000000) {

            br -= 1 << 26;
        }

        opcode = (inst >> 26) & 0b111111;
    }
}


public class Disassembler {

    static String getRegister(int n) {

        if (n == 31) {

            return "XZR";

        } else if (n == 30) {

            return "LR";
        } else if (n == 29) {

            return "FP";


        } else if (n == 28) {

            return "SP";

        } else {

            return "X" + n;
        }
    }

    static int[] instructions = new int[10000];

    static String[] labels = new String[instructions.length];

    static String[] instString = new String[instructions.length];

    static int numInst = 0;

    static int labelCount = 0;

    static String getLabel(int ind) {

        if (labels[ind] == null) {

            labels[ind] = "label" + (++labelCount);
        }

        return labels[ind];
    }


    static String getInstruction(int ind) {

        int inst = instructions[ind];

        RInstruction rInstruction = new RInstruction(inst);

        IInstruction iInstruction = new IInstruction(inst);

        DInstruction dInstruction = new DInstruction(inst);

        CBInstruction cbInstruction = new CBInstruction(inst);

        BInstruction bInstruction = new BInstruction(inst);

	//        System.out.println(iInstruction.opcode + " " + rInstruction.opcode + " " + rInstruction.shamt);

	//        System.out.println(cbInstruction.opcode);

        if (rInstruction.opcode == 1112) {

            return "ADD " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);


        } else if (iInstruction.opcode == 580) {

            return "ADDI " + getRegister(iInstruction.Rd) + ", " + getRegister(iInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (rInstruction.opcode == 0x450) {

            return "AND " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);

        } else if (iInstruction.opcode == 584) {

            return "ANDI " + getRegister(iInstruction.Rd) + ", " + getRegister(iInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (rInstruction.opcode == 0x658) {

            return "SUB " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);


        } else if (iInstruction.opcode == 836) {

            return "SUBI " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (rInstruction.opcode == 0x550) {

            return "ORR " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);

        } else if (iInstruction.opcode == 0x590 / 2) {

            return "ORRI " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (rInstruction.opcode == 0x69B) {

            return "LSL " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + rInstruction.shamt;

        } else if (rInstruction.opcode == 0x69A) {

            return "LSR " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + rInstruction.shamt;

        } else if (rInstruction.opcode == 0x650) {

            return "EOR " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);

        } else if (iInstruction.opcode == 0x690 / 2) {

            return "EORI " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (dInstruction.opcode == 0x7C2) {


            return String.format("LDUR %s, [%s, #%d]", getRegister(dInstruction.Rt), getRegister(dInstruction.Rn), dInstruction.dt);
        } else if (dInstruction.opcode == 0x7C0) {


            return String.format("STUR %s, [%s, #%d]", getRegister(dInstruction.Rt), getRegister(dInstruction.Rn), dInstruction.dt);
        } else if (rInstruction.opcode == 0x758) {

            return "SUBS " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);


        } else if (iInstruction.opcode == 0x788 / 2) {

            return "SUBIS " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + "#" + iInstruction.imm;

        } else if (rInstruction.opcode == 0x4D8) {

            return "MUL " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);

        } else if (rInstruction.opcode == 0x4D6) {

            return "SDIV " + getRegister(rInstruction.Rd) + ", " + getRegister(rInstruction.Rn) + ", " + getRegister(rInstruction.Rm);

        } else if (rInstruction.opcode == 0b11111111101) {

            return "PRNT " + getRegister(rInstruction.Rd);
        } else if (rInstruction.opcode == 0b11111111100) {

            return "PRNL";
        } else if (rInstruction.opcode == 0b11111111110) {

            return "DUMP";
        } else if (rInstruction.opcode == 0b11111111111) {

            return "HALT";
        } else if (cbInstruction.opcode == 0b10110100) {

            return "CBZ " + getRegister(cbInstruction.Rt) + ", " + getLabel(ind + cbInstruction.cond_br);
        } else if (cbInstruction.opcode == 0b10110101) {

            return "CBNZ " + getRegister(cbInstruction.Rt) + ", " + getLabel(ind + cbInstruction.cond_br);
        } else if (rInstruction.opcode == 0b11010110000) {

            return "BR " + getRegister(rInstruction.Rn);
        } else if (bInstruction.opcode == 0b100101) {

            return "BL " + getLabel(ind + bInstruction.br);

        } else if (bInstruction.opcode == 0b000101) {

            return "B " + getLabel(ind + bInstruction.br);

        } else if (cbInstruction.opcode == 0b01010100) {

            String[] conds = new String[]{"EQ", "NE", "HS", "LO", "MI", "PL", "VS", "VC", "HI", "LS", "GE", "LT", "GT", "LE"};

            return String.format("B.%s %s", conds[cbInstruction.Rt], getLabel(ind + cbInstruction.cond_br));

        }


        return "unsupported";
    }


    public static void main(String[] args) {

        if (args.length != 1) {

            System.out.println("usage: java Disassembler <machine file>");

            System.exit(1);
        }

	//        System.out.println(labels[0]);
        try {
	    //            FileInputStream inputStream = new FileInputStream("test.asm.machine");

            FileInputStream inputStream = new FileInputStream(args[0]);

            byte[] bytes = new byte[4];

            while (true) {

                int num = inputStream.read(bytes);

                if (num < 4) {

                    break;
                }

                ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);

                int inst = byteBuffer.getInt();

                instructions[numInst++] = inst;

		//                System.out.println(String.format("%08X", inst));

            }


            for (int i = 0; i < numInst; i++) {

                instString[i] = getInstruction(i);

            }

            for (int i = 0; i < numInst; i++) {

                if (labels[i] != null) {

                    System.out.println(labels[i] + ":");
                }

                System.out.println("  " + instString[i]);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}