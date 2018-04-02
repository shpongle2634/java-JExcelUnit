# JExcelUnit
### Use JUnit Test using Excel.
The JExcelUnit is Eclipse Plug-In Project and it will make the '.xlsx' file which related to the java project you made. 

This Excel File includes some informations about your java project such as class name, constructor, fields, and methods. and those are pre-applied to the cell value as data validation of Excel. So the user can write test case just selecting cell values. These cell values are components of Test case which are **Test Name, Test Target(Class and Constructor), Test Method, Method Inputs, Expected Value, Result and Success/Failure.**

Now, if you want to write your test case, you can write down in your excel, just completing Single line. JExcelUnit can give you some profits. 

* You can write and run your Test Case in this Excel file easy without JUnit Code.
* You can manage your Test Case as a data. and can see test cases more intutive.
* Testing Cost will be reduced rapidly.
* JExcelUnit can read Excel file and run automatically. Also it will record Test Result, Success/Failure and Execution Log.
* You can write Mock Object with it's name in Excel Sheet.
* Available Scenario Test, Unit Test. This can be managed as sheets.

# Installation

1.  Move plugin file(jexcelunit_x.x.jar) to '/dropins' in Eclipse Location. and restart Eclipse.

2.  Go to Eclipse **your Project-Build path-Configure Build Path...-Libraries 탭-add External Jars** and add 'jexcelunit_lib_x.x.jar'.

JExcelUnit Plug-In Download link : https://s3.ap-northeast-2.amazonaws.com/jexcelunit/plugin/jexcelunit_1.0.jar

JExcelUnit Library Download link : https://s3.ap-northeast-2.amazonaws.com/jexcelunit/lib/jexcelunit_lib_1.0.jar


# Usage

1. Create JExcelUnit .xlsx
  Select JExcelUnit Button on Eclipse top bar Or Project-New-Testable Excel .xlsx .  JExcelUnit will analyze your Project and then make Excel File automatically.
  Few Seconds later, you can see the JExcelUnit Runner Class and TestSuite.xlsx.


### Example Video
https://www.youtube.com/watch?v=JYb3gClprCk

### Compare Testing Efficiency between JExcelUnit and JUnit Coding.
https://www.youtube.com/watch?v=pu4lUQhuz7Q

1. Create JExcelUnit .xlsx file.
**Press JExcelUnit button on the top Bar in Eclipse**, Or **Your Project-New-Testable Excel .xlsx** JExcelUnit will analyze your Project and create file automatically. Few seconds later, you can see Test Suite.xlsx and JExcelUnitRunner Class.
![Using new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/Testable+Excel+file.png)
![Using new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/Testable+Excel+file2.png)

2. Write Your Test Case on Excel.
![작성 예시](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image31.png)
3. Run Test Cases using JExcelUnitRunner Class.
![엑셀 생성-new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/JExcelUnit+Runner.png)
4. Check Test Log, Success/Failure on Excel.
![로그와 결과 기록.](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image16.png)
![테스트 시트 예시](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image15.png)

* * *

# JExcelUnit

### 엑셀로 JUnit 테스트를 수행 할 수 있습니다.
JExcelUnit은 이클립스 플러그인 프로젝트이며, 여러분이 만든 자바 프로젝트와 관련된 '.xlsx' 파일을 생성해줍니다.

이 엑셀 파일은 프로젝트를 분석한 정보들을 지니고 있습니다. 클래스 이름, 생성자, 필드, 메소드, 파라미터가 해당됩니다. 이 정보들은 엑셀의 셀 값에 데이터 유효성 검증으로 적용되어있습니다. 따라서 사용자는 테스트 케이스의 구성 요소인 **테스트 이름, 테스트 대상(클래스와 생성자), 테스트 메소드, 입력 값, 예상 값, 결과와 성공여부**를 엑셀 상에서 미리 적용된 셀 값을 선택함으로써 테스트 케이스를 작성 할 수 있습니다.

이제, 여러분이 테스트 케이스를 작성하고 싶다면, JUnit 코드를 작성하지 않고 단지 1 라인의 엑셀을 작성하면 됩니다. JExcelUnit은 여러가지 이점을 줄 수 있습니다.

* JUnit 코드 없이 엑셀에서 테스트 케이스를 작성 및 실행 할 수 있습니다.
* 테스트 케이스를 코드가 아닌 데이터로 관리할 수 있고, 직관적으로 볼 수 있습니다.
* 테스트 비용이 크게 절감 됩니다.
* 엑셀을 읽어 테스트가 자동으로 실행되며, 테스트 결과 및 성공 여부, 테스트 수행 로그가 엑셀에 기록됩니다.
* Mock 객체를 엑셀에 작성하여 사용할 수 있습니다.
* 시나리오 테스트, 단위 테스트가 가능하며 이를 엑셀 Sheet로 관리 할 수 있습니다.


# 설치

1.  jexcelunit_x.x.jar 플러그인을 다운받고 이클립스가 설치된 곳의 '/dropins' 내에 옮기고 이클립스를 재시작합니다.

2.  'jexcelunit_lib_x.x.jar'.를 프로젝트 오른쪽버튼-**Build path-Configure Build Path...-Libraries 탭-add External Jars** 로 추가합니다.

JExcelUnit Plug-In Download link : https://s3.ap-northeast-2.amazonaws.com/jexcelunit/plugin/jexcelunit_1.0.jar

JExcelUnit Library Download link : https://s3.ap-northeast-2.amazonaws.com/jexcelunit/lib/jexcelunit_lib_1.0.jar


# 사용법

### 실행 영상
https://www.youtube.com/watch?v=JYb3gClprCk

### 효율 비교영상
https://www.youtube.com/watch?v=pu4lUQhuz7Q

1. JExcelUnit .xlsx 파일 생성하기
이클립스에서 상단 바의 JExcelUnit 버튼 혹은 프로젝트-New-에서 Testable Excel .xlsx 를 선택하여 JExcelUnit 엑셀파일을 생성합니다. JExcelUnit은 프로젝트를 분석하여 자동으로 파일을 생성해 줍니다. 완료가 된 후에는 프로젝트에서 Test Suite.xlsx 파일과, JExcelUnitRunner class를 확인할 수 있습니다.
![엑셀 생성-new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/Testable+Excel+file.png)
![엑셀 생성-new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/Testable+Excel+file2.png)
2. 테스트 케이스를 엑셀에 작성합니다
![작성 예시](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image31.png)
3. 작성이 완료된 후, JExcelUnitRunner 클래스를 이용하여 테스트를 수행합니다.
![엑셀 생성-new-testable Excel](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/JExcelUnit+Runner.png)
4. 로그와 성공여부를 확인하고 테스트를 추가, 수정하여 수행합니다.
![로그와 결과 기록.](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image16.png)
![테스트 시트 예시](https://s3.ap-northeast-2.amazonaws.com/jexcelunit/image15.png)

