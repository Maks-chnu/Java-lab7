package Java_lab7;

/** Потік руху капітальних будинків: прямолінійний рух до цілі в лівій верхній чверті. */
public class CapitalHouseAI extends BaseAI {

    public CapitalHouseAI(Habitat habitat) {
        super("CapitalHouseAI", habitat);
    }

    @Override
    protected Class<? extends House> objectType() {
        return CapitalHouse.class;
    }

    @Override
    protected void move(House house, double dt) {
        if (house.moveTowardsTarget(Habitat.SPEED, dt)) {
            System.out.println("[" + getName() + "] " + house + " прибув у ліву верхню чверть");
        }
    }
}
