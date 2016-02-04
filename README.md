# chef-api

Jenkins plugin adding Opscode Chef credentials provider /  API to Jenkins. The API is provided by [Apache jclouds](https://jclouds.apache.org/).
To be extended/used by other plugins.

## Installation

* Build: `mvn package`
* Go to `<JENKINS>/pluginManager/advanced` and upload `<THIS_REPO>/target/chef-api.hpi`

## Configuration

* Go to `<JENKINS>/credential-store/domain/_/` and click `Add credentials`
* Choose `Opscode Chef configuration`
* Add the absolute path to a `knife.rb` or `client.rb` file. (You can delete these + any `.pem` keys once the credential has been added successfully)

## Usage 

See [ChefApiBuilder](../blob/master/src/main/java/com/which/hudson/plugins/chef/api/ChefApiBuilder.java)
