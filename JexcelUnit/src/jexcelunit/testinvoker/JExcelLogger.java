package jexcelunit.testinvoker;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.layout.PatternLayout;


public class JExcelLogger {
	private Logger suiteLogger;
	private Logger testLogger;

	public JExcelLogger() {
		ConfigurationBuilder< BuiltConfiguration > builder = ConfigurationBuilderFactory.newConfigurationBuilder();

		builder.setStatusLevel(Level.INFO);
		builder.setConfigurationName("JExcelLoggerConfig");

		//Layout
		LayoutComponentBuilder layout =builder.newLayout("PatternLayout").addAttribute("pattern",PatternLayout.SIMPLE_CONVERSION_PATTERN);

		//Console Appender
		AppenderComponentBuilder appenderBuilder = builder.newAppender("Stdout", "CONSOLE").addAttribute("target",
				ConsoleAppender.Target.SYSTEM_OUT);
		appenderBuilder.add(layout);
		builder.add(appenderBuilder);

		//Test File Appender
		appenderBuilder = builder.newAppender("TEST", "File")
				.addAttribute("fileName", "log/test.log");
		appenderBuilder.add(layout);
		builder.add(appenderBuilder);

		//Suite File Appender
		appenderBuilder = builder.newAppender("SUITE", "File").addAttribute("fileName",
				"log/suite.log");
		appenderBuilder.add(layout);
		builder.add(appenderBuilder);


		// create the new logger
		builder.add( builder.newLogger( "TestLogger", Level.TRACE)
//				.add( builder.newAppenderRef( "Stdout" ))
				.add(builder.newAppenderRef("TEST")));
		builder.add( builder.newLogger( "SuiteLogger", Level.TRACE)
//				.add( builder.newAppenderRef( "Stdout" ))
				.add(builder.newAppenderRef("SUITE")));
		builder.add(builder.newRootLogger(Level.TRACE));
		LoggerContext ctx = Configurator.initialize(builder.build());

		suiteLogger= ctx.getLogger("SuiteLogger");
		testLogger = ctx.getLogger("TestLogger");

	}


	public void testLog(String message){
		testLogger.info(message);
	}

	public void suiteLog(String message){
		suiteLogger.info(message);
	}
	
	public void testFatal(String message){
		testLogger.fatal(message);
	}
	public void suiteFatal(String message){
		suiteLogger.fatal(message);
	}
}
