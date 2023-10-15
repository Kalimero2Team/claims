package com.kalimero2.team.claims.squaremap.config;

import java.util.Collection;

public enum ListMode {
    WHITELIST {
        @Override
        public <E> boolean allowed(final Collection<E> collection, final E e) {
            return collection.contains(e);
        }
    },
    BLACKLIST {
        @Override
        public <E> boolean allowed(final Collection<E> collection, final E e) {
            return !collection.contains(e);
        }
    };

    public abstract <E> boolean allowed(Collection<E> collection, E e);
}
