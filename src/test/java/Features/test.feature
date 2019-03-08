Feature: Login Feature

Verify if user is able to Login in to the site

	Scenario: Add Lender Status Information
		Given user is on the MAIN MENU (J0X01) Screen
		When the user on the MAIN MENU (J0X01) selects COMMON MODULES.
		And the user clicks enter
		Then SUBSYSTEM LEVEL COMMON MODULES (J0X06) screen displays
		When the user selects INSTITUTION DEMOGRAPHIC MENU
		And the user clicks enter
		Then SUBSYSTEM LEVEL INSTITUTION DEMOGRAPHIC MENU (J0X02) screen displays
		When user enters A (Add) in the MODE field
		And user enters TX0J (Lender Status) in the TRANSACTION ID field
		And the user clicks enter
		Then LENDER STATUS SEARCH (TXX2S) screen displays
		When the user enters  the short name for the loan program. in field LOAN PROGRAM
		And the user clicks enter
		Then LENDER STATUS MAINTENANCE (TXX0L) screen displays
		Then message 01021 ADD NECESSARY DATA AN displays at the bottom of the screen
		And the user clicks enter
		And the user clicks enter
		Then message 01004 RECORD SUCCESSFULLY ADDED displays at the bottom of the screen

	Scenario: Change/View Lender Status Information
		Given user is on the MAIN MENU (J0X01) Screen
		When the user on the MAIN MENU (J0X01) selects COMMON MODULES.
		And the user clicks enter
		Then SUBSYSTEM LEVEL COMMON MODULES (J0X06) screen displays
		When the user selects INSTITUTION DEMOGRAPHIC MENU
		And the user clicks enter
		Then SUBSYSTEM LEVEL INSTITUTION DEMOGRAPHIC MENU (J0X02) screen displays
		When user enters C (Change) in the MODE field
		And user enters TX0J (Lender Status) in the TRANSACTION ID field
		And the user clicks enter
		Then LENDER STATUS SEARCH (TXX2S) screen displays
		And the user clicks enter
		Given user is on the LENDER STATUS SELECTION Screen
		When the user on the LENDER STATUS SELECTION selects (TXX0K)
		And the user clicks enter
		Then LENDER STATUS MAINTENANCE (TXX0L) screen displays
		Then message 01022 MAKE DESIRED DATA CHANGE displays at the bottom of the screen
		And the user clicks enter
		And the user clicks enter
		Then message 01005 RECORD SUCCESSFULL displays at the bottom of the screen
