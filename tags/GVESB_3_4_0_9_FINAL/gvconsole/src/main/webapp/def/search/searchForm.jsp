
    <jsp:useBean id="searchForm" class="max.search.SearchForm" scope="session"/>
    <% searchForm.manageBooleans(request); %>
    <jsp:setProperty name="searchForm" property="*"/>

<form name="searchForm" action="<jsp:getProperty name="searchForm" property="actionHTML"/>">
    <input type="hidden" name="resetBooleans">
    <table class="ui-widget-header ui-corner-all">
        <tr>
            <td colspan="2">
                <table class="search">
                    <tr>
                        <td>
                            <nobr>
                                Search:
                                <input type="text" class="input200" name="text" value="<jsp:getProperty name="searchForm" property="textHTML"/>">
                                <input type="submit" class="button"  value="Search...">
                            </nobr>
                            
                            <font class="error"><b><br/><jsp:getProperty name="searchForm" property="errorHTML"/></b></font>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table class="search">
                    <tr>
                        <td>
                            Search type:
                        </td>
                        <td></td>
                        <td>
                                 <nobr><input <%= searchForm.checkSearchType("plainText") %> type="radio" name="searchType" value="plainText"> Plain text</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchType("regularExpression") %> type="radio" name="searchType" value="regularExpression"> Regular expression</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchType("xpath") %> type="radio" name="searchType" value="xpath"> XPath</nobr>
                        </td>
                    </tr>
                </table>
            </td>

            <td rowspan="2">
                <table>
                    <tr>
                        <td>
                            Options:
                        </td>
                        <td></td>
                        <td>
                                 <nobr><input <%= searchForm.checkMatchCase() %> type="checkbox" class= "checkbox" name="matchCase" value="true"> Match case</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchText() %> type="checkbox" class= "checkbox" name="searchText" value="true"> Search text</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchAttributes() %> type="checkbox" class= "checkbox" name="searchAttributes" value="true"> Search attributes</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchAttributeNames() %> type="checkbox" class= "checkbox" name="searchAttributeNames" value="true"> Search attribute names</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchElementNames() %> type="checkbox" class= "checkbox" name="searchElementNames" value="true"> Search element names</nobr>
                            <br/><nobr><input <%= searchForm.checkSearchComments() %> type="checkbox" class= "checkbox" name="searchComments" value="true"> Search comments</nobr>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2">
                <table class="search">
                    <tr>
                        <td>
                            Starting node:
                        </td>
                        <td></td>
                        <td>
                                 <nobr><input <%= searchForm.checkStartingNode("root") %> type="radio" class="radio" name="startingNode" value="root"> Root node</nobr>
                            <br/><nobr><input <%= searchForm.checkStartingNode("current") %> type="radio" class="radio" name="startingNode" value="current"> Current node</nobr>
                        </td>
                    </tr>
                </table>
            </td>

        </tr>
    </table>
</form>
