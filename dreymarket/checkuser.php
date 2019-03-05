<?php

require_once 'db_functions.php';
$db = new DB_Functions();

/*
 *  Endpoint : http://<domain>/dreymarket/checkuser.php
 *  Method : POST
 *  Params : phone
 *  Result : JSON
 */
$response = array();
if(isset($_POST['phone']))
{
    $phone = $_POST['phone'];

    if($db->checkExistUser( $phone))
    {
        $response["exists"] = TRUE;
        echo json_encode($response);
    }
    else
    {
        $response["exists"] = FALSE;
        echo json_encode($response);
    }
}
else
{
    $response["error_msg"] = "Required parameter (phone) is missing!";
    echo json_encode($response);
}

?>
