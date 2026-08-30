package com.reportit.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.reportit.app.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Toggle Map / Heatmap Mode
        binding.toggleMapMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnModeHeatmap) {
                    binding.tvMapDisplayType.text = "🔥 Problem Density Heatmap Layer"
                    binding.tvMapSubtext.text = "Geospatial cluster analysis • 37 issues detected in South Zone"
                    Toast.makeText(requireContext(), "Switched to Geospatial Heatmap View", Toast.LENGTH_SHORT).show()
                } else {
                    binding.tvMapDisplayType.text = "🗺️ Interactive GIS Map View"
                    binding.tvMapSubtext.text = "Center: 12.9716° N, 77.5946° E (Bengaluru)"
                    Toast.makeText(requireContext(), "Switched to Standard Pin View", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Pin 1 interaction
        binding.pin1.setOnClickListener {
            binding.tvMapPinTitle.text = "🛣️ Mysore Road (High Severity Pothole)"
            binding.cardMapPinDetails.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Selected: Mysore Road Pothole", Toast.LENGTH_SHORT).show()
        }

        // Pin 2 interaction
        binding.pin2.setOnClickListener {
            binding.tvMapPinTitle.text = "🗑️ Jayanagar 4th Block (Garbage Cleared)"
            binding.cardMapPinDetails.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Selected: Jayanagar Cleaned Zone", Toast.LENGTH_SHORT).show()
        }

        // Map filter chips
        binding.chipGroupMapFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            Toast.makeText(requireContext(), "Map filters updated (${checkedIds.size} active)", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
