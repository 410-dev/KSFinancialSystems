rootProject.name = "KSFinancialSystems"
include("Services:KSJournalingServer")
findProject("Services:KSJournalingServer")?.name = "KSJournalingServer"
include("Services:KSNotificationServer")
findProject("Services:KSNotificationServer")?.name = "KSNotificationServer"
include("Services:KSServicesInfrastructure")
findProject("Services:LSServicesInfrastructure")?.name = "KSServicesInfrastructure"
include("Applications:KSTraderMachine")
findProject("Applications:KSTraderMachine")?.name = "KSTraderMachine"
include("Applications:KSManualTrade")
findProject("Applications:ManualTrade")?.name = "KSManualTrade"
include("Libraries:KSTraderAPI")
findProject("Libraries:KSTraderAPI")?.name = "KSTraderAPI"
include("Libraries:KSFoundation")
findProject("Libraries:KSFoundation")?.name = "KSFoundation"
include("Libraries:Graphite")
findProject("Libraries:Graphite")?.name = "Graphite"
include("Libraries:liblks")
findProject("Libraries:liblks")?.name = "liblks"
include("Libraries:KSScripting")
findProject("Libraries:KSScripting")?.name = "KSScripting"
include("Drivers:UpBit")
include("Services:KSScriptingInterpreter")
include("Applications:LWCardano")
include("Applications:KSCryptoHedger")