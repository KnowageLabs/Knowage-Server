package it.eng.knowage.tomcatpasswordencryption;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import it.eng.knowage.tomcatpasswordencryption.helper.EncryptedPasswordUtils;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class KnowageTomcatEncryptedPasswordDatasource extends org.apache.tomcat.jdbc.pool.DataSourceFactory {

    private static final Log log = LogFactory.getLog(KnowageTomcatEncryptedPasswordDatasource.class);

    @Override
    public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception {
        try {
            if (obj instanceof Reference ref) {
                StringRefAddr passwordRefAddr = (StringRefAddr) ref.get(PROP_PASSWORD);
                if (passwordRefAddr != null) {
                    String encryptedPwd = (String) passwordRefAddr.getContent();
                    String cleartextPwd = decrypt(encryptedPwd);
                    int index = find(ref);
                    if (index >= 0) {
                        ref.remove(index);
                        ref.add(index, new StringRefAddr(PROP_PASSWORD, cleartextPwd));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to decrypt password. Please check DataSource definition.");
            throw e;
        }
        return super.getObjectInstance(obj, name, nameCtx, environment);
    }

    private int find(Reference ref) {
        Enumeration<RefAddr> enu = ref.getAll();
        for (int i = 0; enu.hasMoreElements(); i++) {
            RefAddr addr = enu.nextElement();
            if (addr.getType().equals(PROP_PASSWORD))
                return i;
        }
        return -1;
    }

    public static String decrypt(String encryptSource) {
        return EncryptedPasswordUtils.decrypt(encryptSource);
    }

}
