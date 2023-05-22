package graphic.animation;

import java.util.*;

public record SpriteAnimation(List<Sprite> sprites, int animationSpeed) implements List<Sprite> {
    public static final int DEFAULT_SPEED = 10;

    public SpriteAnimation(List<Sprite> sprites) {
        this(sprites, DEFAULT_SPEED);
    }

    @Override
    public int size() {
        return sprites.size();
    }

    @Override
    public boolean isEmpty() {
        return sprites.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return sprites.contains(o);
    }

    @Override
    public Iterator<Sprite> iterator() {
        return new SpriteAnimationIterator(this);
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return sprites.toArray(a);
    }

    @Override
    public boolean add(Sprite sprite) {
        return sprites.add(sprite);
    }

    @Override
    public boolean remove(Object o) {
        return sprites.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return new HashSet<>(sprites).containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Sprite> c) {
        return sprites.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Sprite> c) {
        return sprites.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return sprites.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return sprites.retainAll(c);
    }

    @Override
    public void clear() {
        sprites.clear();
    }

    @Override
    public Sprite get(int index) {
        return sprites.get(index);
    }

    @Override
    public Sprite set(int index, Sprite element) {
        return sprites.set(index, element);
    }

    @Override
    public void add(int index, Sprite element) {
        sprites.add(index, element);
    }

    @Override
    public Sprite remove(int index) {
        return sprites.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return sprites.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return sprites.lastIndexOf(o);
    }

        @Override
    public ListIterator<Sprite> listIterator() {
        return sprites.listIterator();
    }

        @Override
    public ListIterator<Sprite> listIterator(int index) {
        return sprites.listIterator(index);
    }

        @Override
    public List<Sprite> subList(int fromIndex, int toIndex) {
        return new SpriteAnimation(sprites.subList(fromIndex, toIndex), animationSpeed);
    }

    public static class SpriteAnimationIterator implements Iterator<Sprite> {
        private final SpriteAnimation sprites;
        private int index;
        private boolean looped;
        public SpriteAnimationIterator(SpriteAnimation sprites) {
            this.sprites = sprites;
        }

        /**
         * Activates or deactivates the iterator loop.
         *
         * @param loop a boolean to active iteration loop.
         */
        public void loop(boolean loop) {
            looped = loop;
        }

        public SpriteAnimationIterator looped() {
            var animIter = new SpriteAnimationIterator(sprites);
            animIter.looped = true;

            return animIter;
        }

        public SpriteAnimationIterator loopedSpriteAnimationIterator(SpriteAnimation sprites) {
            return new SpriteAnimationIterator(sprites).looped();
        }

        @Override
        public boolean hasNext() {
            return looped || index < (sprites.size() - 1);
        }

        @Override
        public Sprite next() throws NoSuchElementException {
            index++;
            if (looped && index >= sprites.size())
                index = 0;

            if (hasNext())
                return sprites.get(index);



            throw new NoSuchElementException("No more sprites in the animation");
        }
    }
}
