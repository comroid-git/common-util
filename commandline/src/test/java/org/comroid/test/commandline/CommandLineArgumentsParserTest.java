package org.comroid.test.commandline;

import org.comroid.commandline.CommandLineArgs;
import org.junit.Assert;
import org.junit.Test;

public class CommandLineArgumentsParserTest {
    private static final String args = "-r --ver 1.15.5 --ciao --adios=amigos -dE";

    @Test
    public void test() {
        final CommandLineArgs parse = CommandLineArgs.parse(args.split(" "));

        Assert.assertTrue(parse.hasFlag('r'));
        Assert.assertTrue(parse.hasFlag('d'));
        Assert.assertTrue(parse.hasFlag('E'));

        Assert.assertFalse(parse.hasFlag('v'));
        Assert.assertFalse(parse.hasFlag('c'));
        Assert.assertFalse(parse.hasFlag('a'));


        Assert.assertTrue(parse.hasKey("ver"));
        Assert.assertEquals("1.15.5", parse.get("ver"));

        Assert.assertTrue(parse.hasKey("ciao"));
        Assert.assertNull(parse.get("ciao"));

        Assert.assertTrue(parse.hasKey("adios"));
        Assert.assertEquals("amigos", parse.get("adios"));
    }
}