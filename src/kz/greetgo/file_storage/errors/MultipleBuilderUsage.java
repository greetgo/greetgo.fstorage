package kz.greetgo.file_storage.errors;

public class MultipleBuilderUsage extends RuntimeException {
  public MultipleBuilderUsage() {
    super("You can use builder only ones. Create new builder and build again");
  }
}
