package com.kingrunes.somnia.common.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;

/**
 * Utility methods for handling lists of WeakReferences and players.
 */
public class ListUtils {

    /**
     * Returns the WeakReference wrapping a given object in a list, or null if not found.
     * Cleans up any cleared references along the way.
     *
     * @param obj  The object to search for
     * @param refs The list of WeakReferences
     * @param <T>  Type of the object
     * @return The WeakReference wrapping obj, or null if not found
     */
    public static <T> WeakReference<T> getWeakRef(T obj, List<WeakReference<T>> refs) {
        Iterator<WeakReference<T>> iter = refs.iterator();
        while (iter.hasNext()) {
            WeakReference<T> weakRef = iter.next();
            T o = weakRef.get();
            if (o == null) {
                iter.remove();
            } else if (o == obj) {
                return weakRef;
            }
        }
        return null;
    }

    /**
     * Checks whether the list contains a WeakReference to the given object.
     * Cleans up cleared references in the process.
     *
     * @param obj  The object to check for
     * @param refs The list of WeakReferences
     * @param <T>  Type of the object
     * @return true if obj is referenced in refs, false otherwise
     */
    public static <T> boolean containsRef(T obj, List<WeakReference<T>> refs) {
        Iterator<WeakReference<T>> iter = refs.iterator();
        while (iter.hasNext()) {
            WeakReference<T> weakRef = iter.next();
            T o = weakRef.get();
            if (o == null) {
                iter.remove();
            } else if (o == obj) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extracts a list of actual objects from a list of WeakReferences, removing any cleared references.
     *
     * @param refs The list of WeakReferences
     * @param <T>  Type of the objects
     * @return A list of live objects
     */
    public static <T> List<T> extractRefs(List<WeakReference<T>> refs) {
        List<T> objects = new ArrayList<>(refs.size());
        Iterator<WeakReference<T>> iter = refs.iterator();
        while (iter.hasNext()) {
            WeakReference<T> weakRef = iter.next();
            T o = weakRef.get();
            if (o == null) {
                iter.remove();
            } else {
                objects.add(o);
            }
        }
        return objects;
    }

    /**
     * Converts a list of EntityPlayerMP objects to an array of their display names.
     *
     * @param players The list of players
     * @return Array of display names
     */
    public static String[] playersToStringArray(List<EntityPlayerMP> players) {
        String[] names = new String[players.size()];
        for (int i = 0; i < players.size(); i++) {
            names[i] = players.get(i)
                .getDisplayName();
        }
        return names;
    }
}
