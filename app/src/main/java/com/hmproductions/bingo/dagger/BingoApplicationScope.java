package com.hmproductions.bingo.dagger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * Created by Harsh Mahajan on 10/05/2018.
 *
 * Retention policy
 */

@Scope
@Retention(RetentionPolicy.CLASS)
public @interface BingoApplicationScope {
}
