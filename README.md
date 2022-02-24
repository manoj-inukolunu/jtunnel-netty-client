# jtunnel-netty
Create Tunnels and expose local servers to the internet , This is still a work in progress

# Java is needed

# Installation
* Checkout source
* `mvn clean install`
* `cd target`
* `java -jar target/jtunnel-netty-1.0-SNAPSHOT.jar tunnel <dest-server> <dest-server-port> <sudomain> <tunnel-db-location> <stats-port>`
* Example invocation `java -jar target/jtunnel-netty-1.0-SNAPSHOT.jar tunnel localhost 3030 manoj /Users/manoj 5050`
* You can see the requests at http://localhost:5050/stats

# Binary
Pre built binary can be found at https://github.com/manoj-inukolunu/jtunnel-netty/releases/tag/v0.5
Download the jar and run
`java -jar jtunnel-netty.jar <subdomain> <localServer Port> <DB Location>`

