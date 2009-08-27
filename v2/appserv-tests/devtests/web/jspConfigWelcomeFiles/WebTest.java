/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 27664 ("Welcome files not found in combination with
 * jsp-property-group").
 *
 * See http://nagoya.apache.org/bugzilla/show_bug.cgi?id=27664 for details.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsp-config-welcome-files";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 27664");
        WebTest webTest = new WebTest(args);
        boolean ok = webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/subdir1/subdir2/");
        ok = ok && webTest.doTest("http://" + webTest.host  + ":" + webTest.port
                       + webTest.contextRoot + "/TestServlet");
        stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
	    stat.printSummary();
    }

    public boolean doTest(String urlString) {

        InputStream is = null;
        BufferedReader input = null;
        boolean status = false;
        try { 
            URL url = new URL(urlString);
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) { 
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                status = false;
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!"Welcome".equals(line)) {
                    System.err.println("Wrong response. Expected: Welcome"
                                       + ", received: " + line);
                    status = false;
                } else {
                    status = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status = false;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }

        return status;
    }

}
