package com.tscript.tscriptc.tree;

import com.tscript.tscriptc.utils.Location;

public interface Tree {

    enum Kind {

    }

    Kind getKind();

    Location getLocation();

    <P, R> R accept(TreeVisitor<P, R> visitor, P p);


    default <P, R> R accept(TreeVisitor<P, R> visitor){
        return accept(visitor, null);
    }

}
