package me.yuge.springwebflux.guava;

import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionTests {

    @Test
    public void whenTransformWithIterable_thenTransformed() {
        Function<String, Integer> function = String::length;

        List<String> names = Lists.newArrayList("John", "Jane", "Adam", "Tom");
        Iterable<Integer> result = names.stream().map(function).collect(Collectors.toList());

        Assert.assertThat(result, Matchers.contains(4, 4, 4, 3));

    }
}
