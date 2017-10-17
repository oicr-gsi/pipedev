package ca.on.oicr.gsi.common.transformation;

import java.util.function.Function;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 *
 * @author mlaszloffy
 */
public class MapStringifier {

	public static <K, V, T extends Set<V>> String transform(Function<K, String> keyTransformer,
			Function<V, String> valueTransformer, Map<K, T> map) {
		return map.entrySet().stream()
				.map(entry -> keyTransformer.apply(entry.getKey()) + "="
						+ entry.getValue().stream().map(valueTransformer).collect(Collectors.joining("&")))
				.collect(Collectors.joining(";"));
	}
	public static <K, V, T extends Set<V>> String transform(Function<K, String> keyTransformer,
			Function<V, String> valueTransformer, SortedMap<K, T> map) {
		return map.entrySet().stream()
				.map(entry -> keyTransformer.apply(entry.getKey()) + "="
						+ entry.getValue().stream().map(valueTransformer).collect(Collectors.joining("&")))
				.collect(Collectors.joining(";"));
	}

}
