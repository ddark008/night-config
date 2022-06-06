package yaml;

import com.electronwill.nightconfig.core.BasicTestEnum;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.yaml.YamlFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;
import static com.electronwill.nightconfig.core.file.FileNotFoundAction.THROW_ERROR;
import static org.junit.jupiter.api.Assertions.*;

public class YamlTest {

	private final File file = new File("test.yml");

	@Test
	public void testReadWrite() {
		Config config = Config.inMemory();
		Config config1 = Config.inMemory();
		Config config2 = Config.inMemory();
		config1.set("foo", "bar");
		config2.set("baz", true);
		config.set("null", null);
		config.set("nullObject", NULL_OBJECT);
		config.set("string", "this is a string");
		config.set("sub.null", null);
		config.set("sub.nullObject", NULL_OBJECT);
		config.set("enum", BasicTestEnum.A); // complex enums doesn't appear to work with SnakeYAML
		config.set("list", Arrays.asList(10, 12));
		config.set("objectList", Arrays.asList(config1, config2));

		System.out.println("Config: " + config);
		System.out.println("classOf[sub] = " + config.get("sub").getClass());
		System.out.println("sub.null = " + config.get("sub.null"));
		System.out.println("sub.nullObject = " + config.get("sub.nullObject"));
		YamlFormat yamlFormat = YamlFormat.defaultInstance();
		yamlFormat.createWriter().write(config, file, WritingMode.REPLACE);

		Config parsed = yamlFormat.createConcurrentConfig();
		yamlFormat.createParser().parse(file, parsed, ParsingMode.REPLACE, THROW_ERROR);
		System.out.println("\nParsed: " + parsed);
		System.out.println("classOf[sub] = " + parsed.get("sub").getClass());
		assertNull(parsed.get("sub.null"));
		assertNull(parsed.get("sub.nullObject"));
		assertSame(NULL_OBJECT, parsed.valueMap().get("null"));
		assertSame(NULL_OBJECT,	parsed.valueMap().get("nullObject"));
		assertEquals(BasicTestEnum.A, parsed.getEnum("enum", BasicTestEnum.class));
		assertEquals(12, parsed.<List<Integer>>get("list").get(1));
		assertEquals(Boolean.TRUE, parsed.<List<UnmodifiableConfig>>get("objectList").get(1).get("baz"));

		Assertions.assertEquals(config, parsed, "Error: written != parsed");
	}
}
