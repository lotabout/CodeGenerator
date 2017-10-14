package me.lotabout.codegenerator.util;

import org.jetbrains.java.generate.element.Element;

public interface MemberEntry<T> extends Element {
    T getRaw();
}
