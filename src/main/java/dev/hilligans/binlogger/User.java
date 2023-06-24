package dev.hilligans.binlogger;

public enum User {

    ENVIRONMENT(0),
    ROLLBACK(1),
    DISPENSER(2),
    DROPPER(3),
    EXPLOSION(4),
    LIGHTNING(5),
    FIRE(6),
    GRAVITY(7),
    PISTON(8),
    LAVA(9),
    WATER(10),
    MOSS(11),
    TREE(12),
    CACTUS(13),
    SUGAR_CANE(14),
    MELON(15),
    PUMPKIN(16),
    SPONGE(17),
    REDSTONE(18);

    final short id;

    User(int id) {
        this.id = (short) id;
    }

    public short getID() {
        return (short) (this.id | (short)(0b11 << 14));
    }
}
