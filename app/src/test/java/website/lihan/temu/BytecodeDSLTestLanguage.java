package website.lihan.temu;

import com.oracle.truffle.api.TruffleLanguage;
import com.oracle.truffle.api.instrumentation.ProvidedTags;
import com.oracle.truffle.api.instrumentation.StandardTags.ExpressionTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootBodyTag;
import com.oracle.truffle.api.instrumentation.StandardTags.RootTag;
import com.oracle.truffle.api.instrumentation.StandardTags.StatementTag;

/**
 * Placeholder language for Bytecode DSL test interpreters.
 */
@ProvidedTags({RootTag.class, RootBodyTag.class, ExpressionTag.class, StatementTag.class})
@TruffleLanguage.Registration(id = BytecodeDSLTestLanguage.ID)
public class BytecodeDSLTestLanguage extends TruffleLanguage<Object> {
    public static final String ID = "BytecodeDSLTestLanguage";

    @Override
    protected Object createContext(Env env) {
        return new Object();
    }

    public static final LanguageReference<BytecodeDSLTestLanguage> REF = LanguageReference.create(BytecodeDSLTestLanguage.class);
}