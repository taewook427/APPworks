// test765 : LocalHub go

package main

import (
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net"
	"net/http"
	"os"
	"path/filepath"
	"sync"
)

// text and file storage
var text Text
var files Files

type Text struct {
	data string
	lock sync.RWMutex
}

type Files struct {
	data []string
	lock sync.RWMutex
}

func main() {
	// init local
	os.RemoveAll("./temp")
	os.MkdirAll("./temp", 0755)
	defer os.RemoveAll("./temp")
	files.data = make([]string, 0)
	log.Println("server initialized")

	// Handle static files
	server := http.FileServer(http.Dir("./"))
	http.Handle("/", server)

	// API endpoints
	http.HandleFunc("/api/text", handleText)
	http.HandleFunc("/api/files/list", handleFileList)
	http.HandleFunc("/api/files/upload", handleFileUpload)
	http.HandleFunc("/api/files/download/", handleFileDownload)
	http.HandleFunc("/api/files/delete/", handleFileDelete)
	log.Println("server starting...")

	// get local ip of server
	addrs, err := net.InterfaceAddrs()
	if err != nil {
		fmt.Println(err)
	}
	for _, addr := range addrs {
		if ipnet, ok := addr.(*net.IPNet); ok && !ipnet.IP.IsLoopback() {
			if ipnet.IP.To4() != nil {
				log.Printf("IP %s:5000", ipnet.IP.String())
			}
		}
	}

	// get connection
	if err := http.ListenAndServe("0.0.0.0:5000", nil); err != nil {
		log.Println(err)
	}
}

// text get & set
func handleText(w http.ResponseWriter, r *http.Request) {
	text.lock.RLock()
	defer text.lock.RUnlock()
	switch r.Method {
	case "GET": // text server -> client
		res := text.data
		w.Write([]byte(res))
	case "POST": // text client -> server
		body, err := io.ReadAll(r.Body)
		if err != nil {
			http.Error(w, "Error reading body", http.StatusBadRequest)
			return
		}
		text.data = string(body)
		w.WriteHeader(http.StatusOK)
	}
}

// file list sync
func handleFileList(w http.ResponseWriter, r *http.Request) {
	if r.Method != "GET" {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	files.lock.RLock()
	defer files.lock.RUnlock()
	json.NewEncoder(w).Encode(files.data)
}

// file upload
func handleFileUpload(w http.ResponseWriter, r *http.Request) {
	if r.Method != "POST" {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	files.lock.RLock()
	defer files.lock.RUnlock()

	// get file format
	r.ParseMultipartForm(10 << 30) // max size 10gb
	file, header, err := r.FormFile("file")
	if err != nil {
		http.Error(w, "Error reading file", http.StatusBadRequest)
		return
	}
	defer file.Close()
	filepath := filepath.Join("temp", header.Filename)

	// Remove existing file if it exists
	if _, err := os.Stat(filepath); err == nil {
		if err := os.Remove(filepath); err != nil {
			http.Error(w, "Error replacing existing file", http.StatusInternalServerError)
			return
		}
	}

	// Save new file
	dst, err := os.Create(filepath)
	defer dst.Close()
	if err != nil {
		http.Error(w, "Error saving file", http.StatusInternalServerError)
		return
	}

	// Copy file in chunks
	buf := make([]byte, 1048576) // 1MB chunks
	if _, err := io.CopyBuffer(dst, file, buf); err != nil {
		http.Error(w, "Error saving file", http.StatusInternalServerError)
		return
	}

	// Update file list
	newFiles := make([]string, 0)
	for _, f := range files.data {
		if f != header.Filename {
			newFiles = append(newFiles, f)
		}
	}
	files.data = append(newFiles, header.Filename) // Add new file
	w.WriteHeader(http.StatusOK)
}

// file download
func handleFileDownload(w http.ResponseWriter, r *http.Request) {
	if r.Method != "GET" {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}

	// get file name & path
	filename := filepath.Base(r.URL.Path)
	filePath := filepath.Join("temp", filename)
	file, err := os.Open(filePath)
	defer file.Close()
	if err != nil {
		http.Error(w, "File not found", http.StatusNotFound)
		return
	}

	// set download mode
	w.Header().Set("Content-Disposition", "attachment; filename="+filename)
	w.Header().Set("Content-Type", "application/octet-stream")

	// buffered sending
	buf := make([]byte, 65536)
	for {
		n, err := file.Read(buf)
		if err != nil {
			if err == io.EOF {
				break
			}
			http.Error(w, "Error reading file", http.StatusInternalServerError)
			return
		}
		w.Write(buf[:n])
		w.(http.Flusher).Flush()
	}
}

// file delete
func handleFileDelete(w http.ResponseWriter, r *http.Request) {
	if r.Method != "DELETE" {
		http.Error(w, "Method not allowed", http.StatusMethodNotAllowed)
		return
	}
	files.lock.Lock()
	defer files.lock.Unlock()

	// Remove from file list
	filename := filepath.Base(r.URL.Path)
	newFiles := make([]string, 0)
	for _, f := range files.data {
		if f != filename {
			newFiles = append(newFiles, f)
		}
	}
	files.data = newFiles

	// Delete file
	os.Remove(filepath.Join("temp", filename))
}
