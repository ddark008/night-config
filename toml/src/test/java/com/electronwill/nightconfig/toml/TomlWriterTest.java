package com.electronwill.nightconfig.toml;

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.TestEnum;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * @author TheElectronWill
 */
public class TomlWriterTest {

	@Test
	public void writeToString() {
		Config subConfig = TomlFormat.instance().createConfig();
		subConfig.set("string", "test");
		subConfig.set("dateTime", ZonedDateTime.now());
		subConfig.set("sub", TomlFormat.instance().createConfig());

		List<Config> tableArray = new ArrayList<>();
		tableArray.add(subConfig);
		tableArray.add(subConfig);
		tableArray.add(subConfig);

		Config config = TomlFormat.instance().createConfig();
		config.set("string", "\"value\"");
		config.set("integer", 2);
		config.set("long", 123456789L);
		config.set("double", 3.1415926535);
		config.set("bool_array", Arrays.asList(true, false, true, false));
		config.set("config", subConfig);
		config.set("table_array", tableArray);
		config.set("enum", TestEnum.A);

		StringWriter stringWriter = new StringWriter();
		TomlWriter writer = new TomlWriter();
		writer.setIndentArrayElementsPredicate(array -> array.size() > 3);
		writer.setWriteTableInlinePredicate(table -> table.size() <= 2);
		writer.write(config, stringWriter);

		System.out.println("Written:");
		System.out.println(stringWriter);
	}

	@Test
	public void correctNewlinesSub() {
		Config conf = TomlFormat.instance().createConfig();
		Config sub = conf.createSubConfig();
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("[table]", "\tkey = \"value\"", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesArrayOfTables() {
		Config conf = TomlFormat.instance().createConfig();

		Config sub = conf.createSubConfig();
		sub.set("key", "value");

		List<Config> arrayOfTables = Arrays.asList(sub);
		conf.set("aot", arrayOfTables);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("[[aot]]", "\tkey = \"value\"", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesSimple() {
		Config conf = TomlFormat.instance().createConfig();
		conf.set("simple", 123);

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("simple = 123", ""), StringUtils.splitLines(written));
	}

	@Test
	public void correctNewlinesMixed() {
		Config conf = TomlFormat.instance().createConfig();
		Config sub = conf.createSubConfig();
		conf.set("simple", 123);
		conf.set("table", sub);
		sub.set("key", "value");

		TomlWriter tWriter = new TomlWriter();
		String written = tWriter.writeToString(conf);
		System.out.println(written);
		assertLinesMatch(Arrays.asList("simple = 123", "", "[table]", "\tkey = \"value\"", ""),
				StringUtils.splitLines(written));
	}

	@Test
	public void noNulls() {
		Config config = TomlFormat.newConfig();
		Executable tryToWrite = () -> TomlFormat.instance().createWriter().writeToString(config);

		config.set("null", null);
		assertThrows(WritingException.class, tryToWrite);

		config.set("null", NullObject.NULL_OBJECT);
		assertThrows(WritingException.class, tryToWrite);
	}
}