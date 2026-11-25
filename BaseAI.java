package Java_lab7;

/**
 * Лабораторна робота №7, завдання 2.
 * Абстрактний клас базової поведінки об'єктів, що працює як окремий потік
 * зі спільною колекцією об'єктів області симуляції.
 *
 * Пауза та відновлення реалізовані керуючим прапорцем і методами монітора
 * wait() / notifyAll() (застарілі suspend() і resume() не використовуються).
 */
public abstract class BaseAI extends Thread {

    protected static final int FRAME_MS = 20;

    protected final Habitat habitat;

    private final Object pauseLock = new Object(); // монітор для паузи
    private volatile boolean running = true;       // volatile — зміна видна всім потокам одразу
    private volatile boolean paused = false;

    protected BaseAI(String name, Habitat habitat) {
        super(name);
        this.habitat = habitat;
        setDaemon(true);
        // Новий потік успадковує пріоритет потоку-творця (EDT має пріоритет 6),
        // тому явно встановлюємо звичайний пріоритет
        setPriority(Thread.NORM_PRIORITY);
    }

    /** Тип об'єктів, якими керує потік. */
    protected abstract Class<? extends House> objectType();

    /** Алгоритм переміщення одного об'єкта за час dt (секунд). */
    protected abstract void move(House house, double dt);

    @Override
    public final void run() {
        long last = System.nanoTime();
        while (running) {
            if (waitWhilePaused()) {
                last = System.nanoTime(); // після паузи не "надолужуємо" пропущений час
            }
            if (!running) {
                break;
            }
            long now = System.nanoTime();
            double dt = (now - last) / 1e9;
            last = now;

            synchronized (habitat.getHouses()) {
                for (House house : habitat.getHouses()) {
                    if (objectType().isInstance(house)) {
                        move(house, dt);
                    }
                }
            }
            try {
                Thread.sleep(FRAME_MS);
            } catch (InterruptedException e) {
                if (!running) {
                    break;
                }
            }
        }
    }

    /** Блокує потік, поки встановлено прапорець паузи. Повертає true, якщо потік чекав. */
    private boolean waitWhilePaused() {
        synchronized (pauseLock) {
            if (!paused) {
                return false;
            }
            while (paused && running) {       // цикл захищає від "хибних пробуджень"
                try {
                    pauseLock.wait();          // звільняє монітор і переводить потік у стан WAITING
                } catch (InterruptedException e) {
                    return true;
                }
            }
            return true;
        }
    }

    /** Призупинити потік (він зупиниться на початку наступного кроку). */
    public void pauseAI() {
        paused = true;
    }

    /** Відновити потік: скидаємо прапорець і будимо потоки, що чекають на моніторі. */
    public void resumeAI() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isPausedAI() {
        return paused;
    }

    /** Коректне завершення потоку замість застарілого stop(). */
    public void shutdown() {
        running = false;
        resumeAI();
        interrupt();
    }
}
