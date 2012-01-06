/**
 * Performs a TermQuery for BUFRUA data.
 * 
 * Usage:
 *   TBD
 *   
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 20080105            713 jkorman     Initial Coding.
 */
function BUFRUARequest(){
  this.plugin = "bufrua";
  this.subscribe = false;
  this.subscription = null;
  this.queryResults = null;
  this.asciiResponse = false;
  this.query = new TermQuery(this.plugin, subscriptionDataFieldId, subscriptionDataQueryId);
}

function _addParameter(name,value,operand){
    if(arguments.length==2){
        this.query.addParameter(name,value);
    } else{
        this.query.addParameter(name,value,operand);
    }
}

function _addList(name, value){
  this.query.addList(name, value);
}

function _setCount(count){
  this.query.setCount(count);
}

function _setSortValue(sortValue){
  this.query.setSortBy(sortValue);
}

function _enableSubscription(){
  this.subscribe = true;
}

function _enableAsciiResponse(){
  this.asciiResponse = true;
}

function _execute()
{
  if(this.subscribe){
    this.subscription = new Subscription();
    this.subscription.setup(this.query);
  }
  
  this.queryResults = this.query.execute();
  if(this.queryResults == null || this.queryResults.size() == 0)
  {
    var response = new MakeResponseNull("Query returned 0 results.",
                                            this.query);
    return response.execute();
  }
  else
  {
    if(this.asciiResponse)
    {
       return this.makeAsciiResponse();
    }
    else
    {
       return this.makeXmlResponse();
    }
  }
}

function _makeXmlResponse()
{
  var xmlResults = new Array();
  var response = new Array();
  for(i=0; i < this.queryResults.size(); i++)
  {    
    var makeResponse = new MakeResponseXml(this.queryResults.get(i));
    response[i] = makeResponse.execute();
  }
  return response;
}

function _makeAsciiResponse()
{
  var xmlResults = new Array();
  var response = new Array();
  for(i=0; i < this.queryResults.size(); i++)
  {    
    var toXml = new DataToXml(this.queryResults.get(i)); 
    xmlResults[i] = toXml.execute();     
    var makeResponse = new MakeResponseAscii(this.queryResults.get(i), xmlResults[i]);
    response[i] = makeResponse.execute(); 
  }
  return response;
}

BUFRUARequest.prototype.execute = _execute;
BUFRUARequest.prototype.makeXmlResponse = _makeXmlResponse;
BUFRUARequest.prototype.makeAsciiResponse = _makeAsciiResponse;
BUFRUARequest.prototype.addParameter = _addParameter;
BUFRUARequest.prototype.addList = _addList;
BUFRUARequest.prototype.setCount = _setCount;
BUFRUARequest.prototype.setSortValue = _setSortValue;
BUFRUARequest.prototype.enableSubscription = _enableSubscription;
BUFRUARequest.prototype.enableAsciiResponse = _enableAsciiResponse;
