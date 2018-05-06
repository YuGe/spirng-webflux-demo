package me.yuge.springwebflux.guava;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JointSplitTests {
    @Test
    public void whenJoinNestedCollections_thenJoined() {
        List<ArrayList<String>> nested = Lists.newArrayList(
                Lists.newArrayList("apple", "banana", "orange"),
                Lists.newArrayList("cat", "dog", "bird"),
                Lists.newArrayList("John", "Jane", "Adam"));

        String result = Joiner.on(";").join(
                nested.stream().map(input -> Joiner.on("-").join(input)).collect(Collectors.toList())
        );

        Assert.assertThat(result, Matchers.containsString("apple-banana-orange"));
        Assert.assertThat(result, Matchers.containsString("cat-dog-bird"));
        Assert.assertThat(result, Matchers.containsString("apple-banana-orange"));
    }
}
