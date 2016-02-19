package com.dinochiesa.testng.tests;

import java.util.HashMap;

public class TestCase {

    private String _testName;
    private String _description;
    private HashMap<String,String>  _input; // JSON hash
    private HashMap<String,String>  _expected; // JSON hash

    // getters
    public String getTestName() { return _testName; }
    public String getDescription() { return _description; }
    public HashMap<String,String> getInput() { return _input; }
    public HashMap<String,String> getExpected() { return _expected; }

    // setters
    public void setTestName(String n) { _testName = n; }
    public void setDescription(String d) { _description = d; }
    public void setInput(HashMap<String,String> hash) { _input = hash; }
    public void setExpected(HashMap<String,String> hash) { _expected = hash; }
}
