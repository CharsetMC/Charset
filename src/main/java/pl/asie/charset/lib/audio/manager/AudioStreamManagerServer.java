package pl.asie.charset.lib.audio.manager;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Random;

public class AudioStreamManagerServer extends AudioStreamManager {
    private final TIntSet sources = new TIntHashSet();
    private final Random random = new Random();

    @Override
    public void put(int source, IAudioStream stream) {
        sources.add(source);
    }

    @Override
    public IAudioStream get(int id) {
        return null;
    }

    @Override
    public int create() {
        int i = random.nextInt();
        while (sources.contains(i)) {
            i++;
        }
        put(i, null);
        return i;
    }

    @Override
    public void remove(int id) {
        sources.remove(id);
    }

    @Override
    public void removeAll() {
        sources.clear();
    }
}
