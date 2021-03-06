/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.console.groovy.DisplayDocs;

/**
 *
 * @author xristosoik (https://github.com/xristosoik)
 */
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class DocStructure {

    private String className = "Not initalized";
    private String classPath;
    private TreeMap<String, MethodStructure> methodList;
    private final String methodSeparator = "<!-- ============ METHOD DETAIL ========== -->";

    public DocStructure(String path, String name) throws Exception {
        className = name;
        classPath = path;
        if (ImportJavadocs.getWebpage()) {
            loadMethodsInfoWeb(path);
        } else {
            loadMethodsInfo(path);
        }
    }

    /**
     * This method loads the documentation of class's all methods.
     *
     * @param path
     */
    protected void loadMethodsInfo(String path) {
        methodList = new TreeMap<String, MethodStructure>();
        boolean find, finishReading = false;
        String temp = "", name;
        MethodStructure temporary;
        int position = 0, counter = 0;
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;
            while ((str = in.readLine()) != null) {
                if (finishReading) {
                    break;
                }
                find = str.contains(methodSeparator);
                if (find) {
                    while ((str = in.readLine()) != null) {
                        if (position == 0 && str.contains("<!--   -->")) {
                            position++;
                            continue;
                        }
                        if (position == 1) {
                            if (str.contains("</a>")) {
                                position++;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 2) {
                            if (str.contains("<ul class=\"blockList\">")) {
                                position++;
                                continue;
                            } else if (str.contains("<ul class=\"blockListLast\">")) {
                                position++;
                                finishReading = true;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 3) {
                            if (str.contains("<li class=\"blockList\">")) {
                                position++;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 4 && str.contains("<h4>")) {
                            name = str;
                            while ((str = in.readLine()) != null) {
                                if (str.contains("</ul>")) {
                                    position = 0;
                                    break;
                                }
                                if (str.contains("</pre>")) {
                                    str += "\n\n";
                                }
                                if (str.contains("</dt>")
                                        || str.contains("<dl>")
                                        || str.contains("</dd>")) {
                                    str = "\n" + str + "\n";
                                }
                                str = str.replaceAll("<br>", "\n");
                                temp += cleanHtmlEntities(
                                        str.replaceAll("\\<.*?>", ""));
                            }
                            name = MethodStructure.cleanName(name);
                            /*
                             I the key exists i add in the end of the name
                             the char - and a number of a counter.
                             */
                            if (methodList.containsKey(name)) {
                                name += "-" + counter;
                                counter++;
                            } else {
                                counter = 0;
                            }
                            temporary = new MethodStructure(name, temp);
                            methodList.put(temporary.getMethodName(), temporary);
                            temp = "";
                        } else {
                            position = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     * Loads the method info from url page
     *
     * @param path The url to the class page.
     */
    protected void loadMethodsInfoWeb(String path) {
        methodList = new TreeMap<String, MethodStructure>();
        boolean find, finishReading = false;
        String temp = "", name;
        MethodStructure temporary;
        int position = 0, counter = 0;
        try {
            URL javadoc = new URL(path);
            URLConnection connection = javadoc.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                if (finishReading) {
                    break;
                }
                find = str.contains(methodSeparator);
                if (find) {
                    while ((str = in.readLine()) != null) {
                        if (position == 0 && str.contains("<!--   -->")) {
                            position++;
                            continue;
                        }
                        if (position == 1) {
                            if (str.contains("</a>")) {
                                position++;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 2) {
                            if (str.contains("<ul class=\"blockList\">")) {
                                position++;
                                continue;
                            } else if (str.contains("<ul class=\"blockListLast\">")) {
                                position++;
                                finishReading = true;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 3) {
                            if (str.contains("<li class=\"blockList\">")) {
                                position++;
                                continue;
                            } else {
                                position = 0;
                            }
                        }
                        if (position == 4 && str.contains("<h4>")) {
                            name = str;
                            while ((str = in.readLine()) != null) {
                                if (str.contains("</ul>")) {
                                    position = 0;
                                    break;
                                }
                                if (str.contains("</pre>")) {
                                    str += "\n\n";
                                }
                                if (str.contains("</dt>")
                                        || str.contains("<dl>")
                                        || str.contains("</dd>")) {
                                    str = "\n" + str + "\n";
                                }
                                str = str.replaceAll("<br>", "\n");
                                temp += cleanHtmlEntities(
                                        str.replaceAll("\\<.*?>", ""));
                            }
                            name = MethodStructure.cleanName(name);
                            /*
                             I the key exists i add in the end of the name
                             the char - and a number of a counter.
                             */
                            if (methodList.containsKey(name)) {
                                name += "-" + counter;
                                counter++;
                            } else {
                                counter = 0;
                            }
                            temporary = new MethodStructure(name, temp);
                            methodList.put(temporary.getMethodName(), temporary);
                            temp = "";
                        } else {
                            position = 0;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    /**
     *
     * @return the name of the class
     */
    public String getClassName() {
        return (className);
    }

    /**
     *
     * @return the path to the class
     */
    public String getClassPath() {
        return (classPath);
    }

    /**
     *
     * @return the Map of the class methods
     */
    public TreeMap<String, MethodStructure> getMethodList() {
        return (methodList);
    }

    /**
     *
     * @param str
     * @return returns the line of html with the correct characters
     */
    public String cleanHtmlEntities(String str) {
        str = str.replaceAll("&nbsp;", " ");
        str = str.replaceAll("&lt;", "<");
        str = str.replaceAll("&gt;", ">");
        str = str.replaceAll("&amp;", "&");
        str = str.replaceAll("&cent;", "¢");
        str = str.replaceAll("&pound;", "£");
        str = str.replaceAll("&yen;", "¥");
        str = str.replaceAll("&euro;", "€");
        str = str.replaceAll("&copy;", "©");
        str = str.replaceAll("&reg;", "®");
        return (str);
    }

    public String getAllDocumentation(String name) {
        name = name.replaceAll("\"", "");
        MethodStructure temp = methodList.get(name);
        String displayedDoc = "", tempName;
        int counter = 0;
        if (temp != null) {
            displayedDoc += temp;
            tempName = name + "-" + counter;
            temp = methodList.get(tempName);
            /*
             The purpose of this loop is to add all the overloaded methods.
             */
            while (temp != null) {
                displayedDoc += temp;
                counter++;
                tempName = name + "-" + counter;
                temp = methodList.get(tempName);
            }
        } else {
            displayedDoc = "There is not method with that name in "
                    + className + " please use \":importdocs\" to import the correct Class";
        }
        return (displayedDoc);
    }
}
