I thought graph based logic represent is more human friendly than code based one. I think graph based test automation more efficient to develop and more ease maintain than code based automation. 
I create an all in one graphical test automation tool to verify this idea. It support user create、organize、run graphical based function test automation. It is named Gate. 

Gate was implemented by combine JMeter and JGraphX together. It use JGraphX to create a test logic flow and it also use many parts of JMeter.
It looks and use like JMeter but it is not just a copy of JMeter. Gate is for function test. Thanks for contributors of JMeter and JGraphX. 

Major function:
- User defined variables.
- Functions.
- Package reusable test flows and include the package to test cases.
- Test fixtures and test case dependency.
- CSV data provider for test case.
- Mutual-thread test execution in test case level.
- Selenium and HTTP/HTTPS request.
- Run in GUI and CMD mode. GUI for develop and CMD for CICD.  

Refer to wiki for this repo for more details