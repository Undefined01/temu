package website.lihan.temu;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.bytecode.BytecodeConfig;
import com.oracle.truffle.api.bytecode.BytecodeLabel;
import com.oracle.truffle.api.bytecode.BytecodeLocal;
import com.oracle.truffle.api.bytecode.BytecodeParser;
import com.oracle.truffle.api.bytecode.BytecodeRootNodes;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.ExpressionTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;

/** Placeholder language for Bytecode DSL test interpreters. */
@ProvidedTags({RootTag.class, RootBodyTag.class, ExpressionTag.class, StatementTag.class})
@TruffleLanguage.Registration(id = BytecodeDSLTestLanguage.ID)
public class BytecodeDSLTestLanguage extends TruffleLanguage<Object> {
  public static final String ID = "BytecodeDSLTestLanguage";

  @Override
  protected CallTarget parse(ParsingRequest request) throws Exception {
    var source = request.getSource();

    BytecodeParser<GettingStartedBytecodeRootNodeGen.Builder> conditionalParser =
        b -> {
          // @formatter:off
          b.beginRoot();
          b.beginBlock();
          BytecodeLocal total = b.createLocal();
          BytecodeLocal i = b.createLocal();

          b.beginStoreLocal(total);
          b.emitLoadConstant(0);
          b.endStoreLocal();

          b.beginStoreLocal(i);
          b.emitLoadConstant(0);
          b.endStoreLocal();

          // Create a label. Labels can only be created in Block/Root operations.
          BytecodeLabel lbl = b.createLabel();

          b.beginWhile();
          b.emitLoadConstant(true);

          b.beginBlock();
          b.beginStoreLocal(i);
          b.beginAdd();
          b.emitLoadLocal(i);
          b.emitLoadConstant(1);
          b.endAdd();
          b.endStoreLocal();

          b.beginIfThen();
          b.beginLessThan();
          b.emitLoadArgument(0);
          b.emitLoadLocal(i);
          b.endLessThan();

          // Branch to the label.
          // Only forward branches are permitted (for backward branches, use While).
          b.emitBranch(lbl);
          b.endIfThen();

          b.beginStoreLocal(total);
          b.beginAdd();
          b.emitLoadLocal(total);
          b.emitLoadLocal(i);
          b.endAdd();
          b.endStoreLocal();
          b.endBlock();
          b.endWhile();

          // Declare the label here. Labels must be emitted in the same operation they are created
          // in.
          b.emitLabel(lbl);

          b.beginReturn();
          b.emitLoadLocal(total);
          b.endReturn();
          b.endBlock();
          b.endRoot();
          // @formatter:on
        };
    BytecodeRootNodes<GettingStartedBytecodeRootNode> rootNodes =
        GettingStartedBytecodeRootNodeGen.create(
            getLanguage(), BytecodeConfig.DEFAULT, conditionalParser);
    GettingStartedBytecodeRootNode checkPassword = rootNodes.getNode(0);

    return checkPassword.getCallTarget();
  }

  @Override
  protected Object createContext(Env env) {
    return new Object();
  }

  private static BytecodeDSLTestLanguage getLanguage() {
    return null;
  }

  public static final LanguageReference<BytecodeDSLTestLanguage> REF =
      LanguageReference.create(BytecodeDSLTestLanguage.class);
}
