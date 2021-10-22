# jtunnel-netty
Create Tunnels and expose local servers to the internet , This is still a work in progress

# Java is needed

# Installation
* Checkout source
* `mvn clean install`
* `java -jar jtunnel-netty.jar <subdomain> <localServer Port> <DB Location>`
* You can see the requests at http://localhost:5050/stats

# Binary
Pre built binary can be found at https://github.com/manoj-inukolunu/jtunnel-netty/releases/tag/v0.5
Download the jar and run
`java -jar jtunnel-netty.jar <subdomain> <localServer Port> <DB Location>`

