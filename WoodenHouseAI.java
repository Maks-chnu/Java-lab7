package Java_lab7;

/** Потік руху дерев'яних будинків: прямолінійний рух до цілі в правій нижній чверті. */
public class WoodenHouseAI extends BaseAI {

    public WoodenHouseAI(Habitat habitat) {
        super("WoodenHouseAI", habitat);
    }

    @Override
    protected Class<? extends House> objectType() {
        return WoodenHouse.class;
    }

    @Override
    protected void move(House house, double dt) {
        if (house.moveTowardsTarget(Habitat.SPEED, dt)) {
            System.out.println("[" + getName() + "] " + house + " прибув у праву нижню чверть");
        }
    }
}
