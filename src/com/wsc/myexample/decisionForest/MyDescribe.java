package com.wsc.myexample.decisionForest;

import com.google.common.collect.Lists;
import org.apache.commons.cli2.CommandLine;
import org.apache.commons.cli2.Group;
import org.apache.commons.cli2.Option;
import org.apache.commons.cli2.OptionException;
import org.apache.commons.cli2.builder.ArgumentBuilder;
import org.apache.commons.cli2.builder.DefaultOptionBuilder;
import org.apache.commons.cli2.builder.GroupBuilder;
import org.apache.commons.cli2.commandline.Parser;
import org.apache.mahout.classifier.df.DFUtils;
import org.apache.mahout.classifier.df.data.DataLoader;
import org.apache.mahout.classifier.df.data.Dataset;
import org.apache.mahout.classifier.df.data.DescriptorException;
import org.apache.mahout.classifier.df.data.DescriptorUtils;
import org.apache.mahout.common.CommandLineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Generates a file descriptor for a given dataset
 */
public final class MyDescribe {

  private static final Logger log = LoggerFactory.getLogger(MyDescribe.class);

  private MyDescribe() {
  }

  public static void main(String[] args) throws IOException, DescriptorException {

    DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    ArgumentBuilder abuilder = new ArgumentBuilder();
    GroupBuilder gbuilder = new GroupBuilder();

    Option pathOpt = obuilder.withLongName("path").withShortName("p").withRequired(true).withArgument(
        abuilder.withName("path").withMinimum(1).withMaximum(1).create()).withDescription("Data path").create();

    Option descriptorOpt = obuilder.withLongName("descriptor").withShortName("d").withRequired(true)
        .withArgument(abuilder.withName("descriptor").withMinimum(1).create()).withDescription(
            "data descriptor").create();

    Option descPathOpt = obuilder.withLongName("file").withShortName("f").withRequired(true).withArgument(
        abuilder.withName("file").withMinimum(1).withMaximum(1).create()).withDescription(
        "Path to generated descriptor file").create();

    Option regOpt = obuilder.withLongName("regression").withDescription("Regression Problem").withShortName("r")
        .create();

    Option helpOpt = obuilder.withLongName("help").withDescription("Print out help").withShortName("h")
        .create();

    Group group = gbuilder.withName("Options").withOption(pathOpt).withOption(descPathOpt).withOption(
        descriptorOpt).withOption(regOpt).withOption(helpOpt).create();

    try {
      Parser parser = new Parser();
      parser.setGroup(group);
      CommandLine cmdLine = parser.parse(args);

      if (cmdLine.hasOption(helpOpt)) {
        CommandLineUtil.printHelp(group);
        return;
      }

      String dataPath = cmdLine.getValue(pathOpt).toString();
      String descPath = cmdLine.getValue(descPathOpt).toString();
      List<String> descriptor = convert(cmdLine.getValues(descriptorOpt));
      boolean regression = cmdLine.hasOption(regOpt);

      log.debug("Data path : {}", dataPath);
      log.debug("Descriptor path : {}", descPath);
      log.debug("Descriptor : {}", descriptor);
      log.debug("Regression : {}", regression);

      runTool(dataPath, descriptor, descPath, regression);
    } catch (OptionException e) {
      log.warn(e.toString());
      CommandLineUtil.printHelp(group);
    }
  }

  private static void runTool(String dataPath, Iterable<String> description, String filePath, boolean regression)
    throws DescriptorException, IOException {
    log.info("Generating the descriptor...");
    String descriptor = DescriptorUtils.generateDescriptor(description);

//    Path fPath = validateOutput(filePath);

    log.info("generating the dataset...");
    MyDataset dataset = generateDataset(descriptor, dataPath, regression);

//    log.info("storing the dataset description");
    MyDFUtils.storeWritable(filePath, dataset);
  }

  private static MyDataset generateDataset(String descriptor, String dataPath, boolean regression) throws IOException,
      DescriptorException {
//    Path path = new Path(dataPath);
//    FileSystem fs = path.getFileSystem(new Configuration());

    return MyDataLoader.generateDataset(descriptor, regression, dataPath);
  }

//  private static Path validateOutput(String filePath) throws IOException {
//    Path path = new Path(filePath);
//    FileSystem fs = path.getFileSystem(new Configuration());
//    if (fs.exists(path)) {
//      throw new IllegalStateException("Descriptor's file already exists");
//    }
//
//    return path;
//  }

  private static List<String> convert(Collection<?> values) {
    List<String> list = Lists.newArrayListWithCapacity(values.size());
    for (Object value : values) {
      list.add(value.toString());
    }
    return list;
  }

}
