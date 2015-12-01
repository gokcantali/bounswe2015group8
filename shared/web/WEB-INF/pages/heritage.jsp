<%@ include file="/WEB-INF/pages/header.jsp" %>

<form class="form-horizontal"
      id="heritageForm"
      action="${contextPath}/upload_heritage"
      method="post"
      enctype="multipart/form-data">
    <div class="form-group">
        <label for="name" class="col-sm-1 control-label">Name</label>
        <div class="col-sm-9">
            <input type="text" class="form-control" name="name" id="name" placeholder="Name">
        </div>
    </div>
    <div class="form-group">
        <label for="place" class="col-sm-1 control-label">Place</label>
        <div class="col-sm-9">
            <input type="text" class="form-control" name="place" id="place" placeholder="Place">
        </div>
        <button class="col-sm-2" type="button" style="background-color: #8a4ce2;" onclick="window.open('/google_map');">
            Choose from Google Maps
            <span style="float:left; font-size: 20px;" class="col-sm-1 glyphicon glyphicon-map-marker"></span>
        </button>
    </div>
    <div class="form-group">
        <label for="place" class="col-sm-1 control-label">Description</label>

        <div class="col-sm-9">
            <textarea class="form-control" rows="3" name="description" id="description" placeholder="Description"
                      style="width: 100%; resize: vertical"></textarea>
        </div>
    </div>
    <div class="form-group">
        <label for="media" class="col-sm-1 control-label">Media</label>
        <div class="col-sm-9">
            <label for="media" class="btn btn-lg"><i class="glyphicon glyphicon-paperclip"></i></label>
            <input type="file" name="media" id="media" style="display:none"/>
        </div>
    </div>
    <div class="form-group">
        <div class="col-sm-offset-1 col-sm-9">
            <button type="submit" class="btn btn-default btn-block">Post</button>
        </div>
    </div>
</form>
<script type="text/javascript">
    function fillPlaceFromGoogleMap(){
        $("#place").val(window['place']);
    }
</script>

<%@ include file="/WEB-INF/pages/footer.jsp" %>




