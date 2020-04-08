package org.comroid.test.uniform;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.comroid.uniform.data.node.UniArrayNode;
import org.comroid.uniform.data.node.UniObjectNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.comroid.uniform.data.impl.json.fastjson.FastJSONLib.fastJsonLib;

public class UniNodeTest {
    private final Random rng = new Random();
    private UniObjectNode<JSON, JSONObject, Integer> object;
    private UniArrayNode<JSON, JSONArray, Integer> array;
    private Map<String, Integer> randomMap;
    private List<Integer> randomInts;

    @Before
    public void setup() {
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();

        randomInts = IntStream.range(0, 50)
                .mapToObj(x -> rng.nextInt(500))
                .distinct()
                .collect(Collectors.toList());
        randomMap = randomInts.stream()
                .collect(Collectors.toMap(x -> {
                    return String.valueOf(x * x);
                }, x -> x));

        object.putAll(randomMap);
        array.addAll(randomInts);

        this.object = fastJsonLib.createUniObjectNode(object);
        this.array = fastJsonLib.createUniArrayNode(array);
    }

    @Test
    public void testObject() {
        object.forEach((key, value) -> Assert.assertEquals(randomMap.get(key), value));
    }

    @Test
    public void testArray() {
        for (int i = 0; i < array.size(); i++) {
            Integer value = array.get(i);

            Assert.assertEquals(randomInts.get(i), value);
        }
    }
}
