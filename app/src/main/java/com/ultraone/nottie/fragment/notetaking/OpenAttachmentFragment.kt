package com.ultraone.nottie.fragment.notetaking

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.PointF
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.VideoView
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.ultraone.nottie.R
import com.ultraone.nottie.databinding.FragmentOpenAttachmentBinding
import com.ultraone.nottie.util.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.properties.Delegates


class OpenAttachmentFragment: Fragment(){
    private lateinit var videoView: VideoView
    var matrix = Matrix()
    var savedMatrix = Matrix()
    var mode = NONE

    // these PointF objects are used to record the point(s) the user is touching
    var start = PointF()
    var mid = PointF()
    var oldDist = 1f
    private val args: OpenAttachmentFragmentArgs by navArgs()
    var binding: FragmentOpenAttachmentBinding  by Delegates.notNull()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOpenAttachmentBinding.inflate(inflater, container, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        when(args.uri.fileType(requireContext())){
            is VIDEO -> {

                handleVideo(args.uri.toUri())

              //  binding.fOAVV.setMediaController()
            }
            is AUDIO -> {
                handleFile(args.uri.toUri())
            }
            is DOCUMENT -> {
                handleFile(args.uri.toUri())
            }
            is IMAGE -> {
                handleImage(args.uri.toUri())
            }
            is Other -> TODO()
        }
        return binding.root
    }
    private fun handleFile(uri: Uri) {

        val mime: String = requireContext().contentResolver.getType(uri)!!

        // Open file with user selected app
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun handleImage(uri: Uri){
        binding.fOAIV.setImageURI(uri)
        binding.fOAIV.setOnTouchListener { v, event ->

            val view = v as ImageView
            view.scaleType = ImageView.ScaleType.MATRIX
            val scale: Float
            val matrix = v.matrix
            dumpEvent(event)
            when (event.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_DOWN -> {
                    matrix.set(view.imageMatrix)
                    savedMatrix.set(matrix)
                    start[event.x] = event.y
                    Log.d(TAG, "mode=DRAG") // write to LogCat
                    mode = DRAG
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    mode = NONE
                    Log.d(TAG, "mode=NONE")
                }
                MotionEvent.ACTION_POINTER_DOWN -> {
                    oldDist = spacing(event)
                    Log.d(TAG, "oldDist=$oldDist")
                    if (oldDist > 5f) {
                        savedMatrix.set(matrix)
                        midPoint(mid, event)
                        mode = ZOOM
                        Log.d(TAG, "mode=ZOOM")
                    }
                }
                MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                    matrix.set(savedMatrix)
                    matrix.postTranslate(
                        event.x - start.x,
                        event.y - start.y
                    ) // create the transformation in the matrix  of points
                } else if (mode == ZOOM) {
                    // pinch zooming
                    val newDist = spacing(event)
                    Log.d(TAG, "newDist=$newDist")
                    if (newDist > 5f) {
                        matrix.set(savedMatrix)
                        scale = newDist / oldDist // setting the scaling of the
                        // matrix...if scale > 1 means
                        // zoom in...if scale < 1 means
                        // zoom out
                        matrix.postScale(scale, scale, mid.x, mid.y)
                    }
                }
            }
            view.imageMatrix = matrix // display the transformation on screen

            true
        }
      //  binding.fOAIV
    }
    private fun handleVideo(uri: Uri){
         binding.fOAVParent.setVisible()
        ::videoView.set(binding.fOAVV)//ad
        (binding.fOAVV).setVisible()
        // binding.fOAVV.resume()
        binding.fOAVV.setVideoURI(uri)
        binding.fOAPlayPause.invokeSelectableState<ImageView> {
            when(it){
                true ->  {
                    binding.fOAVV.start()
                    Log.d(this::class.simpleName, "true")
                    lifecycleScope.launch {
                        delay(1000)
                        binding.fOAPlayPause.setVisible()
                  //      binding.fOAPlayPause.background

                        binding.fOAPlayPause.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                     //   binding.fOAPlayPause.setBackgroundColor(Color.TRANSPARENT)
                     //   binding.fOAPlayPause.isClickable = true
                    }
                }
                false -> {
                    binding.fOAVV.pause()

                    binding.fOAPlayPause.backgroundTintList = ColorStateList.valueOf(requireContext().resolver(R.attr.colorControlNormal))

                    Log.d(this::class.simpleName, "false")
                }
                null -> {

                }
            }
        }
        binding.fOAVV.start()
    }



    /*
     * --------------------------------------------------------------------------
     * Method: spacing Parameters: MotionEvent Returns: float Description:
     * checks the spacing between the two fingers on touch
     * ----------------------------------------------------
     */
    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return Math.sqrt((x * x + y * y).toDouble()).toFloat()
    }

    /*
     * --------------------------------------------------------------------------
     * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
     * Description: calculates the midpoint between the two fingers
     * ------------------------------------------------------------
     */
    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    /** Show an event in the LogCat view, for debugging  */
    private fun dumpEvent(event: MotionEvent) {
        val names = arrayOf(
            "DOWN",
            "UP",
            "MOVE",
            "CANCEL",
            "OUTSIDE",
            "POINTER_DOWN",
            "POINTER_UP",
            "7?",
            "8?",
            "9?"
        )
        val sb = StringBuilder()
        val action = event.action
        val actionCode = action and MotionEvent.ACTION_MASK
        sb.append("event ACTION_").append(names[actionCode])
        if (actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP) {
            sb.append("(pid ").append(action shr MotionEvent.ACTION_POINTER_ID_SHIFT)
            sb.append(")")
        }
        sb.append("[")
        for (i in 0 until event.pointerCount) {
            sb.append("#").append(i)
            sb.append("(pid ").append(event.getPointerId(i))
            sb.append(")=").append(event.getX(i).toInt())
            sb.append(",").append(event.getY(i).toInt())
            if (i + 1 < event.pointerCount) sb.append(";")
        }
        sb.append("]")
        Log.d("Touch Events ---------", sb.toString())
    }

    companion object {
        private const val TAG = "Touch"
        private const val MIN_ZOOM = 1f
        private const val MAX_ZOOM = 1f

        // The 3 states (events) which the user is trying to perform
        const val NONE = 0
        const val DRAG = 1
        const val ZOOM = 2
    }
    override fun onResume() {
        super.onResume()
        if(::videoView.isInitialized)videoView.start()
    }

    override fun onDetach() {
        super.onDetach()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.show(WindowInsets.Type.statusBars())
        }
            else {
                requireActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                    //     WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
    }
}