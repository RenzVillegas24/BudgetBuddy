package com.mooncode.budgetbuddy

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.util.isEmpty
import androidx.core.util.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.Barcode.QR_CODE
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.material.button.MaterialButton
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import qrcode.QRCode
import java.security.MessageDigest
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


class main_transfer : Fragment() {
    private lateinit var txtPhp: MaterialTextView
    private lateinit var txtSavings: MaterialTextView
    private lateinit var llHistory: LinearLayout
    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector

    private lateinit var databaseEvent: ValueEventListener
    private lateinit var viewScanner: AutoFitSurfaceView
    private var colPrimary = 0
    private var colSub = 0
    private var colTextPrimary = 0
    private var colTextSub = 0
    private var colBg = 0

    private var currentMoney = 0.0


    private val secretKey = "tK5UTui+DPh8lIlBxya5XVsmeDCoUl6vHhdIESMB6sQ="
    private val salt = "QWlGNHNhMTJTQWZ2bGhpV3U="
    private val iv = "bVQzNFNhRkQ1Njc4UUFaWA=="



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_main_transfer, container, false)
        llHistory = view.findViewById(R.id.llHistory)
        txtPhp = view.findViewById(R.id.txtPhp)
        txtSavings = view.findViewById(R.id.txtSavings)
        viewScanner = view.findViewById(R.id.viewScanner)
        val imgQR = view.findViewById<ImageView>(R.id.imgQR)
        val btnScan = view.findViewById<MaterialButton>(R.id.btnScan)
        val btnQR = view.findViewById<MaterialButton>(R.id.btnQR)
        val btnCommand = view.findViewById<MaterialButton>(R.id.btnCommand)
        val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)
        val QRTransfer = view.findViewById<LinearLayout>(R.id.QRTransfer)
        val ScanTransfer = view.findViewById<LinearLayout>(R.id.ScanTransfer)
        val cardQR = view.findViewById<CardView>(R.id.cardQR)
        val cardScan = view.findViewById<CardView>(R.id.cardScan)
        val textTransfer = view.findViewById<TextInputEditText>(R.id.textTransfer)
        val txtScan = view.findViewById<TextView>(R.id.txtScan)
        val txtTransferTo = view.findViewById<TextView>(R.id.txtTransferTo)
        val txtTransferToLabel = view.findViewById<TextView>(R.id.txtTransferToLabel)
        val textQRRequest = view.findViewById<TextInputEditText>(R.id.textQRRequest)

        // get colors
        colPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorPrimary, Color.BLACK)
        colSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSecondaryContainer, Color.BLACK)
        colTextPrimary = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnPrimary, Color.BLACK)
        colTextSub = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorOnSecondaryContainer, Color.BLACK)
        colBg = MaterialColors.getColor(requireContext(), com.google.android.material.R.attr.colorSurfaceContainerHigh, Color.BLACK)

        // current mode of transfer
        var mode = 1
        var userTransferUUID = ""

        // initialize detector
        detector = BarcodeDetector.Builder(context).setBarcodeFormats(QR_CODE).build()

        // initialize camera source
        val handler = Handler(Looper.getMainLooper())
        var isProcessing = false

        // set processor
        // this is where the QR code is detected
        detector.setProcessor( object : Detector.Processor<Barcode> {
            override fun release() {}
            override fun receiveDetections(detections: Detector.Detections<Barcode>?) {
                // if the camera is already processing, or if there are no detections, return
                if (isProcessing || detections == null || detections.detectedItems.isEmpty())
                    return

                // Set the flag to prevent further processing for 250ms
                // This is to prevent multiple detections from the same QR code
                isProcessing = true
                handler.postDelayed({
                    isProcessing = false
                }, 250)

                // get the QR code value
                val barcode = detections.detectedItems.valueAt(0)
                val qrCodeValue = barcode.displayValue
                Log.d("QRCode", qrCodeValue ?: "")

                // if the QR code value is not null and starts with "budgetbuddy://transfer?data="
                if (qrCodeValue != null && qrCodeValue.startsWith("budgetbuddy://transfer?data=")) {
                    // get the data, decrypt it, and split it into the user and request
                    val data = qrCodeValue.replace("budgetbuddy://transfer?data=", "").decrypt()
                    val dataArgs = data.split(";")
                    // get the user and request
                    val user = dataArgs[0].replace("USER:", "")
                    val request = dataArgs[1].replace("REQUEST:", "")

                    Log.d("QRCode", "User: $user")
                    Log.d("QRCode", "Request: $request")

                    // set the user to transfer to
                    userTransferUUID = user

                    // run on the UI thread
                    // This is to prevent the app from not responding while the dialog is showing
                    requireActivity().runOnUiThread {
                        if (user == auth!!.uid) {
                            // wait for the dialog to be dismissed
                            cameraSource.stop()
                            // show the dialog
                            MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Error")
                                .setMessage("You cannot transfer to yourself!")
                                .setPositiveButton("OK") { _, _ ->
                                    // if the user has granted camera permission
                                    if (ActivityCompat.checkSelfPermission(
                                            requireContext(),
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                    )
                                        // start the camera again
                                        cameraSource.start(viewScanner.holder)
                                }
                                .setCancelable(false)
                                .show()

                            return@runOnUiThread
                        }

                        // stop the camera
                        cameraSource.stop()
                        cardScan.visibility = View.GONE
                        txtScan.visibility = View.GONE
                        txtTransferTo.visibility = View.VISIBLE
                        txtTransferToLabel.visibility = View.VISIBLE
                        // get the username of the user to transfer to
                        databaseReference!!.child(auth!!.uid!!).child("username").get()
                            .addOnSuccessListener {
                                txtTransferTo.text = "@${it.value.toString()}"
                            }
                        // set the request
                        if (request.isNotEmpty())
                            textTransfer.setText(request)
                    }
                }
            }
        })

        // set the camera source callback
        viewScanner.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                // if the user has granted camera permission
                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED ) {
                    // start the camera
                    cameraSource.start(p0)
                }
                // else request for camera permission
                else requestPermissions( arrayOf(Manifest.permission.CAMERA),1001)
            }
            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {}
            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()

            }

        })


        // random qr code, for testing and showing the qr code while the qr is being decrypted
        val QR = QRCode.ofSquares()
            .withInnerSpacing(0)
            .withColor(colPrimary)
            .withBackgroundColor(Color.TRANSPARENT)
            .build("budgetbuddy://transfer?data=USER:dummy")
            .renderToBytes()
        // set the image view to the qr code
        imgQR.setImageBitmap(BitmapFactory.decodeByteArray(QR, 0, QR.size))

        // switch between qr and scan mode
        fun switchMode() {
            // if the mode is 0, set the scan mode
            if (mode == 0) {
                btnQR.setBackgroundColor(colPrimary)
                btnQR.setTextColor(colTextPrimary)
                btnCommand .visibility = View.GONE
                QRTransfer.visibility = View.VISIBLE

                btnScan.setBackgroundColor(colSub)
                btnScan.setTextColor(colTextSub)
                ScanTransfer.visibility = View.GONE

                // run on another thread
                val thread = Thread(Runnable {
                    // generate the qr code in every change of the text
                    // This is to prevent the app from not responding while the qr code is being generated
                    try {
                        val QR = QRCode.ofSquares()
                            .withInnerSpacing(0)
                            .withColor(colPrimary)
                            .withBackgroundColor(Color.TRANSPARENT)
                            .build("budgetbuddy://transfer?data=${"USER:${auth!!.uid!!};REQUEST:${textQRRequest.text.toString()}".encrypt()}")
                            .renderToBytes()
                        requireActivity().runOnUiThread {
                            imgQR.setImageBitmap(BitmapFactory.decodeByteArray(QR, 0, QR.size))
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                })
                thread.start()

            } else {
                // if the mode is 1, set the qr mode
                btnQR.setBackgroundColor(colSub)
                btnQR.setTextColor(colTextSub)
                btnCommand .visibility = View.VISIBLE
                userTransferUUID = ""
                QRTransfer.visibility = View.GONE

                btnScan.setBackgroundColor(colPrimary)
                btnScan.setTextColor(colTextPrimary)
                ScanTransfer.visibility = View.VISIBLE

            }
        }

        // switch mode on the initialization
        switchMode()

        textQRRequest.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                // switch mode on every change of the text
                switchMode()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // switch to qr mode
        btnQR.setOnClickListener {
            mode = 0
            switchMode()
            cameraSource.stop()

        }

        // back on the previous fragment
        btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // switch to scan mode
        btnScan.setOnClickListener {
            mode = 1
            switchMode()
            cardScan.visibility = View.VISIBLE
            txtScan.visibility = View.VISIBLE
            txtTransferTo .visibility = View.GONE
            txtTransferToLabel.visibility = View.GONE
            textTransfer.setText("")
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED) {
                cameraSource.start(viewScanner.holder)
                Log.d("Camera", "Started")
            }
            else requestPermissions(arrayOf(Manifest.permission.CAMERA), 1001)
        }

        // transfer money
        btnCommand.setOnClickListener {
            // if the amount to transfer is empty
            if (textTransfer.text.toString().isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("Please enter an amount to transfer.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // if the amount to transfer is greater than the current money
            if (textTransfer.text.toString().toDouble() > currentMoney) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("You do not have enough money to transfer.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // if the amount to transfer is less than or equal to 0
            if (textTransfer.text.toString().toDouble() <= 0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("Please enter a valid amount to transfer.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // if the user to transfer to is empty
            if (userTransferUUID.isEmpty()) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("Please scan a QR code to transfer.")
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }

            // show confirmation dialog
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirm Transfer")
                .setMessage("Are you sure you want to transfer ₱${textTransfer.text.toString()} to @${txtTransferTo.text.toString()}?")
                .setPositiveButton("Yes") { _, _ ->
                    btnCommand.isEnabled = false
                    btnBack.isEnabled = false

                    // get the current time
                    val current =
                        LocalDateTime.now().with(LocalTime.MIDNIGHT).atZone(
                            ZoneId.systemDefault()
                        ).toInstant().toEpochMilli()

                    val currentTime =
                        LocalDateTime.now().atZone(
                            ZoneId.systemDefault()
                        ).toInstant().toEpochMilli()

                    // transfer money
                    databaseReference!!
                        .child(auth!!.uid!!)
                        .child("money")
                        .setValue(currentMoney - textTransfer.text.toString().toDouble())
                        .addOnSuccessListener {
                            // add the transfer to your history
                            databaseReference!!.child(auth!!.uid!!).child("history")
                                .child("transfers")
                                .child(current.toString())
                                .child(currentTime.toString())
                                .setValue(hashMapOf(
                                    "value" to textTransfer.text.toString(),
                                    "to" to userTransferUUID
                                ))
                                .addOnSuccessListener {
                                    databaseReference!!.child(userTransferUUID).child("money").get()
                                        .addOnSuccessListener {it2 ->
                                            val currentMoney = it2.value.toString().toDouble()
                                            // add the transfer to the history of the user to transfer to
                                            databaseReference!!
                                                .child(userTransferUUID)
                                                .child("money")
                                                .setValue(currentMoney + textTransfer.text.toString().toDouble())
                                                .addOnSuccessListener {
                                                    databaseReference!!.child(userTransferUUID).child("history")
                                                        .child("transfers")
                                                        .child(current.toString())
                                                        .child(currentTime.toString())
                                                        .setValue(hashMapOf(
                                                            "value" to textTransfer.text.toString(),
                                                            "from" to auth!!.uid!!
                                                        ))
                                                        .addOnSuccessListener {
                                                            MaterialAlertDialogBuilder(requireContext())
                                                                .setTitle("Success")
                                                                .setMessage("You have successfully transferred ₱${textTransfer.text} to @${txtTransferTo.text}.")
                                                                .setPositiveButton("OK") { _, _ ->
                                                                    textTransfer.setText("")
                                                                    // reset the user to transfer to
                                                                    userTransferUUID = ""
                                                                    mode = 1
                                                                    switchMode()
                                                                    // go back to the previous fragment
                                                                    findNavController().popBackStack()
                                                                }
                                                                .show()
                                                        }
                                                }
                                        }
                                }
                        }
                }
                .setNegativeButton("No"){ _, _ ->
                    btnCommand.isEnabled = true
                    btnBack.isEnabled = true
                }
                .show()
        }


        // initialize camera source
        cameraSource = CameraSource.Builder(context,detector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1080,1080)
            .setRequestedFps(30.0f)
            .setAutoFocusEnabled(true)
            .build()

        // initialize database event listener
        databaseEvent = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                update(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        detector.release()
        cameraSource.stop()
        cameraSource.release()
    }

    /*
    * Encryption and Decryption
    * Why do we need to encrypt the QR code?
    * The QR code contains the user to transfer to and the request
    * If we don't encrypt it, there are two problems:
    * 1. The user can change the user to transfer to and the request
    * 2. The user can see the request
    *
    * How does it work?
    * The QR code contains the encrypted data, which is the user to transfer to and the request
    * The data is encrypted using AES-256, which is a symmetric encryption algorithm
    * The key is generated using PBKDF2WithHmacSHA1, which is a key derivation function
    * The key is generated using the secret key and the salt, which is a random string
    * The secret key is generated using the SHA-256 hash of the secret key.
    * The SHA-256 hash of the secret key is used to prevent the secret key from being used directly
    * Once the data is encrypted, it is encoded using Base64, which is a binary-to-text encoding scheme
     */

    // encrypt function for the string class
    private fun String.encrypt(): String {
        // initialize the iv parameter spec
        // this is to prevent the same encrypted string from having the same output
        val ivParameterSpec = IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
        // initialize the secret key factory
        // this is to generate the secret key
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        // initialize the spec
        // this is to generate the secret key
        val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
        // generate the secret key
        val tmp = factory.generateSecret(spec)
        // initialize the secret key
        val secretKey =  SecretKeySpec(tmp.encoded, "AES")
        // initialize the cipher
        // this is to encrypt the string
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        // initialize the cipher
        // use the secret key and iv parameter spec
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParameterSpec)

        // return the encrypted string
        return Base64.encodeToString(cipher.doFinal(this.toByteArray(Charsets.UTF_8)), Base64.DEFAULT)
    }

    // decrypt function for the string class
    private fun String.decrypt(): String {
        // initialize the iv parameter spec
        // this is to prevent the same encrypted string from having the same output
        val ivParameterSpec =  IvParameterSpec(Base64.decode(iv, Base64.DEFAULT))
        // initialize the secret key factory
        // this is to generate the secret key
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        // initialize the spec
        // this is to generate the secret key
        val spec =  PBEKeySpec(secretKey.toCharArray(), Base64.decode(salt, Base64.DEFAULT), 10000, 256)
        // generate the secret key
        val tmp = factory.generateSecret(spec)
        // initialize the secret key
        // this is to decrypt the string
        val secretKey =  SecretKeySpec(tmp.encoded, "AES")
        // initialize the cipher
        val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
        // initialize the cipher
        // use the secret key and iv parameter spec
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParameterSpec)
        // return the decrypted string
        return  String(cipher.doFinal(Base64.decode(this, Base64.DEFAULT)))
    }


    override fun onStart() {
        txtPhp.alpha = 0f
        txtSavings.alpha = 0f

        databaseReference!!
            .child(auth!!.uid!!)
            .addValueEventListener(databaseEvent)

        Log.d("Firebase: Transfer", "Event Listener added")

        super.onStart()
    }

    override fun onStop() {
        databaseReference!!
            .child(auth!!.uid!!)
            .removeEventListener(databaseEvent)

        Log.d("Firebase: Transfer", "Event Listener removed")

        super.onStop()
    }

    // update the ui
    fun update(it: DataSnapshot){
        // update the current money
        currentMoney = it.child("money").value.toString().toDouble()
        txtSavings.text = "%,.2f".format(it.child("money").value.toString().toDouble())
        txtSavings.animate().alpha(1f).setDuration(500).setInterpolator(
            AccelerateInterpolator()
        ).start()
        txtPhp.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()

        // update the history
        llHistory.removeAllViews()
        llHistory.alpha = 0f

        it.child("history").child("transfers").children.forEach {ith ->
            val histDate = ith.key.toString().toLong()

            val histDateView = TextView(requireActivity())
            histDateView.text = SimpleDateFormat("MMMM dd, yyyy").format(histDate)
            histDateView.textSize = 25f
            histDateView.setTextColor(colPrimary)
            histDateView.setPadding(0, 40, 0, 0)
            llHistory.addView(histDateView)


            ith.children.forEach{itdt ->
                val histTime = itdt.key.toString().toLong()
                val histValue = itdt.child("value").value.toString().toDouble()



                val llTm = LinearLayout(requireActivity())
                llTm.orientation = LinearLayout.HORIZONTAL
                llTm.setPadding(0, 10, 0, 0)

                val histTimeView = TextView(requireActivity())
                histTimeView.text = SimpleDateFormat("hh:mm a").format(histTime)
                histTimeView.textSize = 20f
                histTimeView.typeface = Typeface.create("sans-serif", Typeface.BOLD)
                histTimeView.setPadding(0, 0, 20, 0)
                llTm.addView(histTimeView)

                val histValueView = TextView(requireActivity())
                histValueView.text = "• ₱ %,.2f".format(histValue)
                histValueView.textSize = 20f
                llTm.addView(histValueView)


                val histTransView = TextView(requireActivity())
                histTransView.textSize = 20f
                histTransView.setPadding(0, 0, 0, 0)

                if (itdt.child("from").exists()){
                    databaseReference!!
                        .child(itdt.child("from").value.toString())
                        .child("username")
                        .get()
                        .addOnSuccessListener {
                            histTransView.text =  "From: @${it.value.toString()}"

                        }
                } else {
                    databaseReference!!
                        .child(itdt.child("to").value.toString())
                        .child("username")
                        .get()
                        .addOnSuccessListener {
                            histTransView.text =  "To: @${it.value.toString()}"

                        }
                }

                llHistory.addView(llTm)
                llHistory.addView(histTransView)

                llHistory.animate().alpha(1f).setDuration(500).setInterpolator(AccelerateInterpolator()).start()


            }

        }

    }


}