<?php

class AutomatonFile {
    public $id;
    public $fileName = '';
    public $filePath = '';
}

if (is_ajax()) {
    if (isset($_POST["action"]) && !empty($_POST["action"])) { //Checks if action value exists
        $action = $_POST["action"];
        switch ($action) { //Switch case for value of action
            case "aFiles":
                getAutomatonFiles();
                break;
        }
    }
}

//Function to check if the request is an AJAX request
function is_ajax()
{
    return isset($_SERVER['HTTP_X_REQUESTED_WITH']) && strtolower($_SERVER['HTTP_X_REQUESTED_WITH']) == 'xmlhttprequest';
}

function getAutomatonFiles()
{
    $autoDir = '../data/automatonFiles';
    $fileNames = array();

    $counter = -1;
    foreach (glob($autoDir . '/*Automaton.json') as $file) {
        $automatonFile = new AutomatonFile();
        $automatonFile->fileName = basename($file);
        $automatonFile->filePath = $file;
        $automatonFile->id = ++$counter;
        $fileNames[] = $automatonFile;
    }
    echo json_encode($fileNames);
}


